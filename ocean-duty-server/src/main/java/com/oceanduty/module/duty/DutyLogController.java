package com.oceanduty.module.duty;

import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.module.duty.domain.DutyLogDTO;
import com.oceanduty.module.duty.domain.DutyLogQueryDTO;
import com.oceanduty.module.duty.domain.DutyLogDetailVO;
import com.oceanduty.module.duty.domain.DutyLogRecordResultVO;
import com.oceanduty.module.duty.domain.DutyLogSnapshotStatusVO;
import com.oceanduty.module.duty.domain.DutyLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 值班日志接口
 */
@Tag(name = "值班日志")
@RestController
@RequiredArgsConstructor
public class DutyLogController {

    private final DutyLogService dutyLogService;
    private final DutyLogSnapshotService dutyLogSnapshotService;

    @Operation(summary = "仪表盘记录日志按钮状态 @author ocean-duty")
    @GetMapping("/duty/log/snapshot-status")
    public ResponseDTO<DutyLogSnapshotStatusVO> getSnapshotStatus() {
        return dutyLogSnapshotService.getSnapshotStatus();
    }

    @Operation(summary = "记录当前监控异常快照 @author ocean-duty")
    @PostMapping("/duty/log/record-snapshot")
    public ResponseDTO<DutyLogRecordResultVO> recordSnapshot() {
        return dutyLogSnapshotService.recordSnapshot();
    }

    @Operation(summary = "值班日志详情 @author ocean-duty")
    @GetMapping("/duty/log/detail/{id}")
    public ResponseDTO<DutyLogDetailVO> getDutyLogDetail(@PathVariable Long id) {
        return dutyLogService.getDutyLogDetail(id);
    }

    @Operation(summary = "分页查询值班日志 @author ocean-duty")
    @PostMapping("/duty/log/query")
    public ResponseDTO<PageResultVO<DutyLogVO>> queryDutyLog(@Valid @RequestBody DutyLogQueryDTO queryDTO) {
        return dutyLogService.queryDutyLog(queryDTO);
    }

    @Operation(summary = "新增值班日志 @author ocean-duty")
    @PostMapping("/duty/log/add")
    public ResponseDTO<String> addDutyLog(@Valid @RequestBody DutyLogDTO dutyLogDTO) {
        return dutyLogService.addDutyLog(dutyLogDTO);
    }

    @Operation(summary = "更新值班日志 @author ocean-duty")
    @PostMapping("/duty/log/update")
    public ResponseDTO<String> updateDutyLog(@Valid @RequestBody DutyLogDTO dutyLogDTO) {
        return dutyLogService.updateDutyLog(dutyLogDTO);
    }

    @Operation(summary = "删除值班日志 @author ocean-duty")
    @GetMapping("/duty/log/delete/{id}")
    public ResponseDTO<String> deleteDutyLog(@PathVariable Long id) {
        return dutyLogService.deleteDutyLog(id);
    }
}
