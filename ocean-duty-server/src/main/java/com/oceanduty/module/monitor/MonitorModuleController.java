package com.oceanduty.module.monitor;

import com.oceanduty.common.anno.RequireRole;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.constant.UserRoleConst;
import com.oceanduty.module.monitor.domain.MonitorModuleDTO;
import com.oceanduty.module.monitor.domain.MonitorModuleQueryDTO;
import com.oceanduty.module.monitor.domain.MonitorModuleVO;
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
 * 监控模块管理接口
 */
@Tag(name = "监控模块管理")
@RestController
@RequireRole(UserRoleConst.ADMIN)
@RequiredArgsConstructor
public class MonitorModuleController {

    private final MonitorModuleManageService monitorModuleManageService;

    @Operation(summary = "分页查询监控模块 @author ocean-duty")
    @PostMapping("/monitor/module/query")
    public ResponseDTO<PageResultVO<MonitorModuleVO>> queryModule(@Valid @RequestBody MonitorModuleQueryDTO queryDTO) {
        return monitorModuleManageService.queryModule(queryDTO);
    }

    @Operation(summary = "查询监控模块详情 @author ocean-duty")
    @GetMapping("/monitor/module/get/{id}")
    public ResponseDTO<MonitorModuleVO> getModule(@PathVariable Long id) {
        return monitorModuleManageService.getModule(id);
    }

    @Operation(summary = "新增监控模块 @author ocean-duty")
    @PostMapping("/monitor/module/add")
    public ResponseDTO<String> addModule(@Valid @RequestBody MonitorModuleDTO moduleDTO) {
        return monitorModuleManageService.addModule(moduleDTO);
    }

    @Operation(summary = "更新监控模块 @author ocean-duty")
    @PostMapping("/monitor/module/update")
    public ResponseDTO<String> updateModule(@Valid @RequestBody MonitorModuleDTO moduleDTO) {
        return monitorModuleManageService.updateModule(moduleDTO);
    }

    @Operation(summary = "删除监控模块 @author ocean-duty")
    @GetMapping("/monitor/module/delete/{id}")
    public ResponseDTO<String> deleteModule(@PathVariable Long id) {
        return monitorModuleManageService.deleteModule(id);
    }
}
