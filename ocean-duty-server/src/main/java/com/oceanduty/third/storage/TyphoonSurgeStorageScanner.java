package com.oceanduty.third.storage;

import com.oceanduty.config.TyphoonSurgeProperties;
import com.oceanduty.module.monitor.MonitorDatasourceDao;
import com.oceanduty.module.monitor.TyphoonSurgeDatasourceIds;
import com.oceanduty.module.monitor.domain.MonitorDatasourceEntity;
import com.oceanduty.util.CredentialEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 台风风暴潮 FTP 与共享目录扫描（凭据从 monitor_datasource 读取并解密）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TyphoonSurgeStorageScanner {

    private static final Pattern YEAR_DIR_PATTERN = Pattern.compile("^\\d{4}$");
    private static final String RAW_SHARE_SCAN_DIR = "ty_surge/result";

    private final TyphoonSurgeProperties properties;
    private final MonitorDatasourceDao monitorDatasourceDao;
    private final CredentialEncryptUtil credentialEncryptUtil;

    public FileScanResult scanFtp() {
        TyphoonSurgeProperties.Ftp ftpProps = properties.getFtp();
        if (!ftpProps.isEnabled()) {
            return FileScanResult.disabled("FTP 扫描已禁用");
        }
        MonitorDatasourceEntity ftpDs = monitorDatasourceDao.selectById(TyphoonSurgeDatasourceIds.FTP);
        String baseDir = resolveBaseDir(ftpDs, ftpProps.getBaseDir());
        if (useFtpLocalMount(ftpProps)) {
            return scanLatestYearFile(Path.of(ftpProps.getMountBase()).resolve(trimLeadingSlash(baseDir)));
        }
        if (ftpDs == null) {
            return FileScanResult.error("未配置台风风暴潮 FTP 数据源");
        }
        String password = decryptPassword(ftpDs.getPassword());
        if (!StringUtils.hasText(password)) {
            return FileScanResult.error("FTP 数据源密码未配置");
        }
        FTPClient client = new FTPClient();
        try {
            client.setConnectTimeout(ftpProps.getConnectTimeoutMs());
            client.setDataTimeout(ftpProps.getDataTimeoutMs());
            client.connect(ftpDs.getHost(), ftpDs.getPort() == null ? 21 : ftpDs.getPort());
            if (!client.login(ftpDs.getUsername(), password)) {
                return FileScanResult.error("FTP 登录失败");
            }
            client.enterLocalPassiveMode();
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            return scanLatestYearFileRemote(client, baseDir);
        } catch (Exception e) {
            log.warn("台风风暴潮 FTP 扫描失败: {}", e.getMessage());
            return FileScanResult.error(e.getMessage());
        } finally {
            disconnectQuietly(client);
        }
    }

    public FileScanResult scanRawShare() {
        TyphoonSurgeProperties.Share shareProps = properties.getShare();
        if (!shareProps.isEnabled()) {
            return FileScanResult.disabled("共享目录扫描已禁用");
        }
        MonitorDatasourceEntity shareDs = monitorDatasourceDao.selectById(TyphoonSurgeDatasourceIds.SHARE);
        String subDir = resolveRawShareScanDir(shareDs, shareProps);
        if (StringUtils.hasText(shareProps.getMountBase())) {
            Path base = Path.of(shareProps.getMountBase());
            if (StringUtils.hasText(subDir)) {
                base = base.resolve(subDir);
            }
            return scanLatestYearLatestFolderFile(base);
        }
        return scanRawShareRemote(shareDs, subDir);
    }

    private String resolveRawShareScanDir(MonitorDatasourceEntity shareDs, TyphoonSurgeProperties.Share shareProps) {
        String configured = shareDs != null && StringUtils.hasText(shareDs.getTableName())
                ? shareDs.getTableName()
                : shareProps.getSubDir();
        if (!StringUtils.hasText(configured) || "ty_surge".equals(configured)) {
            return RAW_SHARE_SCAN_DIR;
        }
        return configured;
    }

    private FileScanResult scanRawShareRemote(MonitorDatasourceEntity shareDs, String subDir) {
        if (shareDs == null) {
            return FileScanResult.error("未配置台风风暴潮共享数据源");
        }
        if (!StringUtils.hasText(shareDs.getDatabaseName())) {
            return FileScanResult.error("共享数据源未配置共享名");
        }
        String password = decryptPassword(shareDs.getPassword());
        if (!StringUtils.hasText(password)) {
            return FileScanResult.error("共享数据源密码未配置");
        }
        SMBClient client = new SMBClient();
        try (Connection connection = client.connect(shareDs.getHost())) {
            AuthenticationContext auth = new AuthenticationContext(
                    shareDs.getUsername(), password.toCharArray(), null);
            Session session = connection.authenticate(auth);
            try (DiskShare share = (DiskShare) session.connectShare(shareDs.getDatabaseName())) {
                return scanLatestYearLatestFolderFileRemote(share, subDir);
            }
        } catch (Exception e) {
            log.warn("台风风暴潮共享目录扫描失败: {}", e.getMessage());
            return FileScanResult.error(e.getMessage());
        }
    }

    private FileScanResult scanLatestYearLatestFolderFileRemote(DiskShare share, String baseDir) {
        String normalizedBase = normalizeSmbPath(baseDir);
        Optional<String> latestYear = findLatestYearDirRemote(share, normalizedBase);
        if (latestYear.isEmpty()) {
            return FileScanResult.empty();
        }
        String yearPath = joinSmbPath(normalizedBase, latestYear.get());
        Optional<String> latestFolder = findLatestSubFolderRemote(share, yearPath);
        if (latestFolder.isEmpty()) {
            return findLatestFileRemote(share, yearPath, latestYear.get());
        }
        String folderPath = joinSmbPath(yearPath, latestFolder.get());
        String folderLabel = latestYear.get() + "/" + latestFolder.get();
        return findLatestFileRemote(share, folderPath, folderLabel);
    }

    private Optional<String> findLatestYearDirRemote(DiskShare share, String baseDir) {
        return listChildNames(share, baseDir).stream()
                .filter(name -> YEAR_DIR_PATTERN.matcher(name).matches())
                .max(Comparator.naturalOrder());
    }

    private Optional<String> findLatestSubFolderRemote(DiskShare share, String yearDir) {
        return listChildNames(share, yearDir).stream()
                .filter(name -> isDirectory(share, joinSmbPath(yearDir, name)))
                .max(Comparator.comparing(this::folderSortKey));
    }

    private FileScanResult findLatestFileRemote(DiskShare share, String dir, String folderLabel) {
        return listEntries(share, dir).stream()
                .filter(entry -> !isDirectoryEntry(entry))
                .max(Comparator.comparing(this::readSmbModifiedTime, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(entry -> FileScanResult.of(
                        folderLabel,
                        entry.getFileName(),
                        readSmbModifiedTime(entry),
                        entry.getEndOfFile()))
                .orElse(FileScanResult.empty());
    }

    private List<String> listChildNames(DiskShare share, String path) {
        return listEntries(share, path).stream()
                .map(FileIdBothDirectoryInformation::getFileName)
                .filter(name -> !".".equals(name) && !"..".equals(name))
                .toList();
    }

    private List<FileIdBothDirectoryInformation> listEntries(DiskShare share, String path) {
        try {
            return share.list(path);
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean isDirectory(DiskShare share, String path) {
        try {
            return share.folderExists(path);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDirectoryEntry(FileIdBothDirectoryInformation entry) {
        long attributes = entry.getFileAttributes();
        return (attributes & 0x10) != 0;
    }

    private LocalDateTime readSmbModifiedTime(FileIdBothDirectoryInformation entry) {
        if (entry.getLastWriteTime() == null) {
            return null;
        }
        return LocalDateTime.ofInstant(entry.getLastWriteTime().toInstant(), ZoneId.systemDefault());
    }

    private String normalizeSmbPath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String joinSmbPath(String base, String child) {
        if (!StringUtils.hasText(base)) {
            return normalizeSmbPath(child);
        }
        if (!StringUtils.hasText(child)) {
            return normalizeSmbPath(base);
        }
        return normalizeSmbPath(base) + "/" + normalizeSmbPath(child);
    }

    private String resolveBaseDir(MonitorDatasourceEntity ftpDs, String fallback) {
        if (ftpDs != null && StringUtils.hasText(ftpDs.getTableName())) {
            return ftpDs.getTableName();
        }
        return fallback;
    }

    private String decryptPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return null;
        }
        return credentialEncryptUtil.decrypt(password);
    }

    private FileScanResult scanLatestYearFile(Path baseDir) {
        if (!Files.isDirectory(baseDir)) {
            return FileScanResult.error("目录不存在: " + baseDir);
        }
        try {
            Optional<Path> latestYearDir = findLatestYearDirLocal(baseDir);
            if (latestYearDir.isEmpty()) {
                return FileScanResult.empty();
            }
            return findLatestFileLocal(latestYearDir.get(), latestYearDir.get().getFileName().toString());
        } catch (IOException e) {
            return FileScanResult.error(e.getMessage());
        }
    }

    private FileScanResult scanLatestYearLatestFolderFile(Path baseDir) {
        if (!Files.isDirectory(baseDir)) {
            return FileScanResult.error("目录不存在: " + baseDir);
        }
        try {
            Optional<Path> latestYearDir = findLatestYearDirLocal(baseDir);
            if (latestYearDir.isEmpty()) {
                return FileScanResult.empty();
            }
            Optional<Path> latestFolder = findLatestSubFolderLocal(latestYearDir.get());
            if (latestFolder.isEmpty()) {
                return findLatestFileLocal(latestYearDir.get(), latestYearDir.get().getFileName().toString());
            }
            String folderLabel = latestYearDir.get().getFileName() + "/" + latestFolder.get().getFileName();
            return findLatestFileLocal(latestFolder.get(), folderLabel);
        } catch (IOException e) {
            return FileScanResult.error(e.getMessage());
        }
    }

    private FileScanResult scanLatestYearFileRemote(FTPClient client, String baseDir) throws IOException {
        Optional<String> latestYear = findLatestYearDirRemote(client, baseDir);
        if (latestYear.isEmpty()) {
            return FileScanResult.empty();
        }
        String yearPath = joinPath(baseDir, latestYear.get());
        FTPFile[] files = client.listFiles(yearPath);
        if (files == null) {
            return FileScanResult.empty();
        }
        return Arrays.stream(files)
                .filter(FTPFile::isFile)
                .max(Comparator.comparing(file -> toLocalDateTime(file.getTimestamp()), Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(file -> FileScanResult.of(latestYear.get(), file.getName(), toLocalDateTime(file.getTimestamp()), readFileSize(file)))
                .orElse(FileScanResult.empty());
    }

    private Optional<Path> findLatestYearDirLocal(Path baseDir) throws IOException {
        try (var stream = Files.list(baseDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(path -> YEAR_DIR_PATTERN.matcher(path.getFileName().toString()).matches())
                    .max(Comparator.comparing(path -> path.getFileName().toString()));
        }
    }

    private Optional<Path> findLatestSubFolderLocal(Path yearDir) throws IOException {
        try (var stream = Files.list(yearDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .max(Comparator.comparing(path -> folderSortKey(path.getFileName().toString())));
        }
    }

    private String folderSortKey(String folderName) {
        int separator = folderName.lastIndexOf('_');
        if (separator > 0 && separator < folderName.length() - 1) {
            return folderName.substring(separator + 1);
        }
        return folderName;
    }

    private FileScanResult findLatestFileLocal(Path dir, String folderLabel) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .max(Comparator.comparing(this::readModifiedTime, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .map(path -> FileScanResult.of(
                            folderLabel,
                            path.getFileName().toString(),
                            readModifiedTime(path),
                            readFileSize(path)))
                    .orElse(FileScanResult.empty());
        }
    }

    private Optional<String> findLatestYearDirRemote(FTPClient client, String baseDir) throws IOException {
        FTPFile[] folders = client.listFiles(baseDir);
        if (folders == null) {
            return Optional.empty();
        }
        return Arrays.stream(folders)
                .filter(FTPFile::isDirectory)
                .map(FTPFile::getName)
                .filter(name -> YEAR_DIR_PATTERN.matcher(name).matches())
                .max(Comparator.naturalOrder());
    }

    private boolean useFtpLocalMount(TyphoonSurgeProperties.Ftp ftp) {
        if (!StringUtils.hasText(ftp.getMountBase())) {
            return false;
        }
        Path base = Path.of(ftp.getMountBase());
        return Files.isDirectory(base) && Files.isReadable(base);
    }

    private String trimLeadingSlash(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String joinPath(String base, String child) {
        if (!StringUtils.hasText(base)) {
            return child;
        }
        if (base.endsWith("/")) {
            return base + child;
        }
        return base + "/" + child;
    }

    private LocalDateTime readModifiedTime(Path path) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            return null;
        }
    }

    private Long readFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return null;
        }
    }

    private Long readFileSize(FTPFile file) {
        if (file == null) {
            return null;
        }
        long size = file.getSize();
        return size >= 0 ? size : null;
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

    public record FileScanResult(String folder, String fileName, LocalDateTime modifiedTime, Long fileSizeBytes,
                                 String errorMessage) {

        static FileScanResult of(String folder, String fileName, LocalDateTime modifiedTime, Long fileSizeBytes) {
            return new FileScanResult(folder, fileName, modifiedTime, fileSizeBytes, null);
        }

        static FileScanResult empty() {
            return new FileScanResult(null, null, null, null, null);
        }

        static FileScanResult disabled(String message) {
            return new FileScanResult(null, null, null, null, message);
        }

        static FileScanResult error(String message) {
            return new FileScanResult(null, null, null, null, message);
        }
    }
}