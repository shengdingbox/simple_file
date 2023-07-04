package com.free.fs.controller.request;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限表实体
 *
 * @author dinghao
 * @date 2021/3/12
 */
@Data
@TableName("file_type")
@EqualsAndHashCode(callSuper = true)
public class FileTypeRequest extends Model<FileTypeRequest> {

    private Long id;
    private String storageType;
    private String domainUrl;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String bucketName;
    private String token;
    private String region;
    private String localFilePath;
    private String source;
    private org.springframework.ui.Model model;
}
