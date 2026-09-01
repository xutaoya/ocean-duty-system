package com.oceanduty.module.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 台风风暴潮网站库记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyphoonSurgeMysqlRecord {

    private LocalDateTime initialTime;

    private LocalDateTime updateTime;
}
