package com.zhouzifei.mediaTool.modules.file.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 文件位置处理类
 */
@Data
public class FilePositionBO implements Serializable {

    private static final long serialVersionUID = -2301680106151135087L;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名称
     */
    private String filename;
}
