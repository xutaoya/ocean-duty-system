package com.oceanduty.module.monitor;

import com.oceanduty.common.domain.PageResultVO;
import com.oceanduty.common.domain.ResponseDTO;
import com.oceanduty.module.monitor.domain.MonitorSiteDTO;
import com.oceanduty.module.monitor.domain.MonitorSiteQueryDTO;
import com.oceanduty.module.monitor.domain.MonitorSiteVO;
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
 * 网站管理接口
 */
@Tag(name = "网站管理")
@RestController
@RequiredArgsConstructor
public class MonitorSiteController {

    private final MonitorSiteManageService monitorSiteManageService;

    @Operation(summary = "分页查询网站 @author ocean-duty")
    @PostMapping("/monitor/site/query")
    public ResponseDTO<PageResultVO<MonitorSiteVO>> querySite(@Valid @RequestBody MonitorSiteQueryDTO queryDTO) {
        return monitorSiteManageService.querySite(queryDTO);
    }

    @Operation(summary = "查询网站详情 @author ocean-duty")
    @GetMapping("/monitor/site/get/{id}")
    public ResponseDTO<MonitorSiteVO> getSite(@PathVariable Long id) {
        return monitorSiteManageService.getSite(id);
    }

    @Operation(summary = "新增网站 @author ocean-duty")
    @PostMapping("/monitor/site/add")
    public ResponseDTO<String> addSite(@Valid @RequestBody MonitorSiteDTO siteDTO) {
        return monitorSiteManageService.addSite(siteDTO);
    }

    @Operation(summary = "更新网站 @author ocean-duty")
    @PostMapping("/monitor/site/update")
    public ResponseDTO<String> updateSite(@Valid @RequestBody MonitorSiteDTO siteDTO) {
        return monitorSiteManageService.updateSite(siteDTO);
    }

    @Operation(summary = "删除网站 @author ocean-duty")
    @GetMapping("/monitor/site/delete/{id}")
    public ResponseDTO<String> deleteSite(@PathVariable Long id) {
        return monitorSiteManageService.deleteSite(id);
    }
}
