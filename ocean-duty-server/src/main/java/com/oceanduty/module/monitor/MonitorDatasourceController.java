package com.oceanduty.module.monitor;

import com.oceanduty.common.anno.RequireRole;
import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.constant.UserRoleConst;
import com.oceanduty.module.monitor.domain.MonitorDatasourceDTO;
import com.oceanduty.module.monitor.domain.MonitorDatasourceQueryDTO;
import com.oceanduty.module.monitor.domain.MonitorDatasourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模块数据源管理接口
 */
@Tag(name = "模块数据源管理")
@RestController
@RequireRole(UserRoleConst.ADMIN)
@RequiredArgsConstructor
public class MonitorDatasourceController {

    private final MonitorDatasourceManageService monitorDatasourceManageService;

    @Operation(summary = "分页查询数据源 @author ocean-duty")
    @PostMapping("/monitor/datasource/query")
    public ResponseDTO<PageResultVO<MonitorDatasourceVO>> queryDatasource(
            @Valid @RequestBody MonitorDatasourceQueryDTO queryDTO) {
        return monitorDatasourceManageService.queryDatasource(queryDTO);
    }

    @Operation(summary = "查询全部启用数据源 @author ocean-duty")
    @GetMapping("/monitor/datasource/list")
    public ResponseDTO<List<MonitorDatasourceVO>> listDatasource() {
        return monitorDatasourceManageService.listAllDatasource();
    }

    @Operation(summary = "查询数据源详情 @author ocean-duty")
    @GetMapping("/monitor/datasource/get/{id}")
    public ResponseDTO<MonitorDatasourceVO> getDatasource(@PathVariable Long id) {
        return monitorDatasourceManageService.getDatasource(id);
    }

    @Operation(summary = "新增数据源 @author ocean-duty")
    @PostMapping("/monitor/datasource/add")
    public ResponseDTO<String> addDatasource(@Valid @RequestBody MonitorDatasourceDTO dto) {
        return monitorDatasourceManageService.addDatasource(dto);
    }

    @Operation(summary = "更新数据源 @author ocean-duty")
    @PostMapping("/monitor/datasource/update")
    public ResponseDTO<String> updateDatasource(@Valid @RequestBody MonitorDatasourceDTO dto) {
        return monitorDatasourceManageService.updateDatasource(dto);
    }

    @Operation(summary = "删除数据源 @author ocean-duty")
    @GetMapping("/monitor/datasource/delete/{id}")
    public ResponseDTO<String> deleteDatasource(@PathVariable Long id) {
        return monitorDatasourceManageService.deleteDatasource(id);
    }

    @Operation(summary = "测试数据源连接 @author ocean-duty")
    @PostMapping("/monitor/datasource/test/{id}")
    public ResponseDTO<String> testDatasource(@PathVariable Long id) {
        return monitorDatasourceManageService.testDatasource(id);
    }
}
