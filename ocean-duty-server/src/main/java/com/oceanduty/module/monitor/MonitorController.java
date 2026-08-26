package com.oceanduty.module.monitor;

import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.module.monitor.domain.DashboardVO;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 监控管理接口
 */
@Tag(name = "监控管理")
@RestController
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorQueryService monitorQueryService;
    private final MonitorCheckService monitorCheckService;
    private final MonitorModuleCheckService monitorModuleCheckService;

    @Value("${ocean-duty.monitor.module-check-enabled:false}")
    private boolean moduleCheckEnabled;

    @Operation(summary = "获取监控仪表盘数据 @author ocean-duty")
    @GetMapping("/monitor/dashboard/get")
    public ResponseDTO<DashboardVO> getDashboard() {
        return ResponseDTO.succ(monitorQueryService.getDashboard());
    }

    @Operation(summary = "查询全部网站监控 @author ocean-duty")
    @GetMapping("/monitor/site/list")
    public ResponseDTO<List<MonitorSiteVO>> listSites() {
        return ResponseDTO.succ(monitorQueryService.listAllSites());
    }

    @Operation(summary = "查询全部模块监控 @author ocean-duty")
    @GetMapping("/monitor/module/list")
    public ResponseDTO<List<MonitorModuleVO>> listModules() {
        return ResponseDTO.succ(monitorQueryService.listAllModules());
    }

    @Operation(summary = "手动触发网站检测 @author ocean-duty")
    @PostMapping("/monitor/site/check")
    public ResponseDTO<String> checkSites() {
        monitorCheckService.checkAllSites();
        return ResponseDTO.succ("检测任务已执行");
    }

    @Operation(summary = "手动触发模块检测 @author ocean-duty")
    @PostMapping("/monitor/module/check")
    public ResponseDTO<String> checkModules() {
        if (!moduleCheckEnabled) {
            return ResponseDTO.succ("模块更新时间检测暂未启用");
        }
        monitorModuleCheckService.checkAllModules();
        return ResponseDTO.succ("模块检测任务已执行");
    }
}
