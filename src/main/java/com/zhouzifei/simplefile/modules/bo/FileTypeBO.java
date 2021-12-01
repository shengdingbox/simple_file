package com.zhouzifei.simplefile.modules.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件位置处理类
 */
@Data
public class FileTypeBO implements Serializable {

    private static final long serialVersionUID = -2301680106151135087L;

    /**
     * 文件ID
     */
    private String lable;

    /**
     * 文件名称
     */
    private String value;
}
