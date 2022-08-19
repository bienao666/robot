package com.bienao.robot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 节假日
 */
@Data
@AllArgsConstructor
public class Festival {
    /**
     * 名称
     */
    private String name;
    /**
     * 月份（阴历节日写阴历月份）
     */
    private int month;
    /**
     * 日期（阴历节日写阴历日期）
     */
    private int day;
    /**
     * 是否阴历节日
     */
    private boolean chineseDate;
    /**
     * 天数
     */
    private Long diff;
}

