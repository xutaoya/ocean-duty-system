package com.oceanduty.common.domain;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageParamDTO {

    /**
     * 页码，从1开始
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    public Integer getPageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
