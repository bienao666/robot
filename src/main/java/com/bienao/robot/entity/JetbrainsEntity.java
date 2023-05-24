package com.bienao.robot.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @description jetbrains
 * @author bienao
 * @date 2023-05-24
 */
@Data
public class JetbrainsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Integer id;

    /**
     * url
     */
    private String url;

    /**
     * created_time
     */
    private String createdTime;

    /**
     * updated_time
     */
    private String updatedTime;
}
