package com.oceanduty.third.ftp;

import com.oceanduty.config.SmartGridFtpProperties;
import com.oceanduty.module.monitor.SmartGridElementCatalog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能网格 FTP / 本地挂载目录扫描
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmartGridFtpScanner {

    private static final Pattern FOLDER_TIME_PATTERN = Pattern.compile("^\\d{10}$");
    private static final DateTimeFormatter TEN_DIGIT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter TWELVE_DIGIT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final SmartGridFtpProperties ftpProperties;

    /**
     * 批量扫描全部要素（复用同一 FTP 连接或本地挂载）
     */
    public Map<String, FtpScanResult> scanAll(List<SmartGridElementCatalog.ElementDef> elements) {
        if (!ftpProperties.isEnabled()) {
            return elements.stream()
                    .collect(Collectors.toMap(SmartGridElementCatalog.ElementDef::key, element -> FtpScanResult.disabled()));
        }
        if (useLocalMount()) {
            return scanAllLocal(elements);
        }
        return scanAllRemote(elements);
    }

    /**
     * 扫描单个要素的 Output 与要素目录文件时间
     */
    public FtpScanResult scan(SmartGridElementCatalog.ElementDef element) {
        return scanAll(List.of(element)).get(element.key());
    }

    private Map<String, FtpScanResult> scanAllLocal(List<SmartGridElementCatalog.ElementDef> elements) {
        Path base = Path.of(ftpProperties.getMountBase());
        Map<String, FtpScanResult> results = new java.util.HashMap<>();
        for (SmartGridElementCatalog.ElementDef element : elements) {
            try {
                Optional<OutputFileInfo> output = element.scanOutput()
                        ? findLatestOutputLocal(base.resolve(element.outputDir()), element)
                        : Optional.empty();
                Optional<ElementFileInfo> elementFile = findLatestElementLocal(base.resolve(element.elementDir()), element);
                results.put(element.key(), FtpScanResult.of(output.orElse(null), elementFile.orElse(null)));
            } catch (Exception e) {
                log.warn("本地扫描智能网格目录失败: element={}, msg={}", element.key(), e.getMessage());
                results.put(element.key(), FtpScanResult.error(e.getMessage()));
            }
        }
        return results;
    }

    private Map<String, FtpScanResult> scanAllRemote(List<SmartGridElementCatalog.ElementDef> elements) {
        if (!StringUtils.hasText(ftpProperties.getPassword())) {
            FtpScanResult error = FtpScanResult.error("未配置 GRID_FTP_PASSWORD");
            return elements.stream().collect(Collectors.toMap(SmartGridElementCatalog.ElementDef::key, element -> error));
        }
        FTPClient client = new FTPClient();
        Map<String, FtpScanResult> results = new java.util.HashMap<>();
        try {
            client.setConnectTimeout(ftpProperties.getConnectTimeoutMs());
            client.setDataTimeout(ftpProperties.getDataTimeoutMs());
            client.connect(ftpProperties.getHost(), ftpProperties.getPort());
            if (!client.login(ftpProperties.getUsername(), ftpProperties.getPassword())) {
                FtpScanResult error = FtpScanResult.error("FTP 登录失败");
                return elements.stream().collect(Collectors.toMap(SmartGridElementCatalog.ElementDef::key, element -> error));
            }
            client.enterLocalPassiveMode();
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            for (SmartGridElementCatalog.ElementDef element : elements) {
                try {
                    Optional<OutputFileInfo> output = element.scanOutput()
                            ? findLatestOutputRemote(client, element)
                            : Optional.empty();
                    Optional<ElementFileInfo> elementFile = findLatestElementRemote(client, element);
                    results.put(element.key(), FtpScanResult.of(output.orElse(null), elementFile.orElse(null)));
                } catch (Exception e) {
                    log.warn("FTP 扫描智能网格目录失败: element={}, msg={}", element.key(), e.getMessage());
                    results.put(element.key(), FtpScanResult.error(e.getMessage()));
                }
            }
            return results;
        } catch (Exception e) {
            log.warn("FTP 连接失败: msg={}", e.getMessage());
            FtpScanResult error = FtpScanResult.error(e.getMessage());
            return elements.stream().collect(Collectors.toMap(SmartGridElementCatalog.ElementDef::key, element -> error));
        } finally {
            disconnectQuietly(client);
        }
    }

    private boolean useLocalMount() {
        if (!StringUtils.hasText(ftpProperties.getMountBase())) {
            return false;
        }
        Path base = Path.of(ftpProperties.getMountBase());
        return Files.isDirectory(base) && Files.isReadable(base);
    }

    private Optional<OutputFileInfo> findLatestOutputLocal(Path outputDir,
                                                           SmartGridElementCatalog.ElementDef element) throws IOException {
        if (!Files.isDirectory(outputDir)) {
            return Optional.empty();
        }
        try (var stream = Files.list(outputDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> toOutputFileInfo(path.getFileName().toString(), path, element))
                    .filter(info -> info != null && info.dataTime() != null)
                    .max(Comparator.comparing(OutputFileInfo::dataTime));
        }
    }

    private Optional<ElementFileInfo> findLatestElementLocal(Path elementDir,
                                                             SmartGridElementCatalog.ElementDef element) throws IOException {
        if (!Files.isDirectory(elementDir)) {
            return Optional.empty();
        }
        if (element.elementLayout() == SmartGridElementCatalog.ElementLayout.FLAT) {
            return findLatestFlatElementLocal(elementDir, element);
        }
        return findLatestSubdirElementLocal(elementDir, element);
    }

    private Optional<ElementFileInfo> findLatestFlatElementLocal(Path elementDir,
                                                                 SmartGridElementCatalog.ElementDef element) throws IOException {
        try (var stream = Files.list(elementDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matchesElementFile(path.getFileName().toString(), element))
                    .map(path -> toElementFileInfo(element.elementDir(), path.getFileName().toString(), path, element))
                    .filter(info -> info != null && info.dataTime() != null)
                    .max(Comparator.comparing(ElementFileInfo::dataTime));
        }
    }

    private Optional<ElementFileInfo> findLatestSubdirElementLocal(Path elementDir,
                                                                   SmartGridElementCatalog.ElementDef element) throws IOException {
        Optional<String> latestFolder;
        try (var stream = Files.list(elementDir)) {
            latestFolder = stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> FOLDER_TIME_PATTERN.matcher(name).matches())
                    .max(Comparator.naturalOrder());
        }
        if (latestFolder.isEmpty()) {
            return Optional.empty();
        }
        String folder = latestFolder.get();
        Path folderPath = elementDir.resolve(folder);
        try (var stream = Files.list(folderPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> toElementFileInfo(folder, path.getFileName().toString(), path, element))
                    .filter(info -> info != null && info.dataTime() != null)
                    .max(Comparator.comparing(ElementFileInfo::dataTime));
        }
    }

    private Optional<OutputFileInfo> findLatestOutputRemote(FTPClient client,
                                                              SmartGridElementCatalog.ElementDef element) throws IOException {
        FTPFile[] files = client.listFiles(element.outputDir());
        if (files == null) {
            return Optional.empty();
        }
        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().startsWith(element.outputPrefix()))
                .map(file -> toOutputFileInfo(file.getName(), file, element))
                .filter(info -> info != null && info.dataTime() != null)
                .max(Comparator.comparing(OutputFileInfo::dataTime));
    }

    private Optional<ElementFileInfo> findLatestElementRemote(FTPClient client,
                                                              SmartGridElementCatalog.ElementDef element) throws IOException {
        if (element.elementLayout() == SmartGridElementCatalog.ElementLayout.FLAT) {
            return findLatestFlatElementRemote(client, element);
        }
        return findLatestSubdirElementRemote(client, element);
    }

    private Optional<ElementFileInfo> findLatestFlatElementRemote(FTPClient client,
                                                                  SmartGridElementCatalog.ElementDef element) throws IOException {
        FTPFile[] files = client.listFiles(element.elementDir());
        if (files == null) {
            return Optional.empty();
        }
        return Arrays.stream(files)
                .filter(file -> file.isFile() && matchesElementFile(file.getName(), element))
                .map(file -> toElementFileInfo(element.elementDir(), file.getName(), file, element))
                .filter(info -> info != null && info.dataTime() != null)
                .max(Comparator.comparing(ElementFileInfo::dataTime));
    }

    private Optional<ElementFileInfo> findLatestSubdirElementRemote(FTPClient client,
                                                                    SmartGridElementCatalog.ElementDef element) throws IOException {
        FTPFile[] folders = client.listFiles(element.elementDir());
        if (folders == null) {
            return Optional.empty();
        }
        Optional<String> latestFolder = Arrays.stream(folders)
                .filter(FTPFile::isDirectory)
                .map(FTPFile::getName)
                .filter(name -> FOLDER_TIME_PATTERN.matcher(name).matches())
                .max(Comparator.naturalOrder());
        if (latestFolder.isEmpty()) {
            return Optional.empty();
        }
        String folder = latestFolder.get();
        String folderPath = element.elementDir() + "/" + folder;
        FTPFile[] files = client.listFiles(folderPath);
        if (files == null) {
            return Optional.empty();
        }
        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().endsWith(".nc"))
                .map(file -> toElementFileInfo(folder, file.getName(), file, element))
                .filter(info -> info != null && info.dataTime() != null)
                .max(Comparator.comparing(ElementFileInfo::dataTime));
    }

    private OutputFileInfo toOutputFileInfo(String fileName, Path path, SmartGridElementCatalog.ElementDef element) {
        LocalDateTime dataTime = parseOutputTime(fileName, element.outputTimePattern());
        if (dataTime == null) {
            return null;
        }
        LocalDateTime modifiedTime = readModifiedTime(path);
        return new OutputFileInfo(fileName, dataTime, modifiedTime);
    }

    private OutputFileInfo toOutputFileInfo(String fileName, FTPFile file, SmartGridElementCatalog.ElementDef element) {
        LocalDateTime dataTime = parseOutputTime(fileName, element.outputTimePattern());
        if (dataTime == null) {
            return null;
        }
        return new OutputFileInfo(fileName, dataTime, toLocalDateTime(file.getTimestamp()));
    }

    private ElementFileInfo toElementFileInfo(String folder, String fileName, Path path,
                                              SmartGridElementCatalog.ElementDef element) {
        LocalDateTime dataTime = parseElementTime(fileName, element.elementTimePattern());
        if (dataTime == null) {
            return null;
        }
        return new ElementFileInfo(folder, fileName, dataTime, readModifiedTime(path));
    }

    private ElementFileInfo toElementFileInfo(String folder, String fileName, FTPFile file,
                                              SmartGridElementCatalog.ElementDef element) {
        LocalDateTime dataTime = parseElementTime(fileName, element.elementTimePattern());
        if (dataTime == null) {
            return null;
        }
        return new ElementFileInfo(folder, fileName, dataTime, toLocalDateTime(file.getTimestamp()));
    }

    private boolean matchesElementFile(String fileName, SmartGridElementCatalog.ElementDef element) {
        if (fileName.endsWith(".ffs_tmp") || fileName.equals("sync.ffs_db")) {
            return false;
        }
        if (!fileName.endsWith(element.elementFileSuffix())) {
            return false;
        }
        if (StringUtils.hasText(element.elementFilePrefix()) && !fileName.startsWith(element.elementFilePrefix())) {
            return false;
        }
        return true;
    }

    private LocalDateTime parseOutputTime(String fileName, Pattern pattern) {
        if (pattern == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.find()) {
            return null;
        }
        return parseTenDigits(matcher.group(1));
    }

    private LocalDateTime parseElementTime(String fileName, Pattern pattern) {
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.find()) {
            return null;
        }
        if (matcher.groupCount() >= 4) {
            try {
                return LocalDateTime.of(
                        Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)),
                        Integer.parseInt(matcher.group(4)),
                        0,
                        0);
            } catch (NumberFormatException | java.time.DateTimeException e) {
                return null;
            }
        }
        String digits = matcher.group(1);
        if (digits.length() == 12) {
            return parseTwelveDigits(digits);
        }
        if (digits.length() == 10) {
            return parseTenDigits(digits);
        }
        return null;
    }

    private LocalDateTime parseTenDigits(String digits) {
        try {
            return LocalDateTime.parse(digits, TEN_DIGIT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseTwelveDigits(String digits) {
        try {
            return LocalDateTime.parse(digits, TWELVE_DIGIT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime readModifiedTime(Path path) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(java.util.Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }

    private void disconnectQuietly(FTPClient client) {
        if (client == null || !client.isConnected()) {
            return;
        }
        try {
            client.logout();
        } catch (IOException ignored) {
            // ignore
        }
        try {
            client.disconnect();
        } catch (IOException ignored) {
            // ignore
        }
    }

    public record OutputFileInfo(String fileName, LocalDateTime dataTime, LocalDateTime modifiedTime) {
    }

    public record ElementFileInfo(String folder, String fileName, LocalDateTime dataTime, LocalDateTime modifiedTime) {
    }

    public record FtpScanResult(OutputFileInfo output, ElementFileInfo element, String errorMessage) {

        static FtpScanResult of(OutputFileInfo output, ElementFileInfo element) {
            return new FtpScanResult(output, element, null);
        }

        static FtpScanResult disabled() {
            return new FtpScanResult(null, null, "FTP 扫描已禁用");
        }

        static FtpScanResult error(String message) {
            return new FtpScanResult(null, null, message);
        }
    }
}
