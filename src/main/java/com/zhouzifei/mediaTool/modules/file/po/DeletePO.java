package com.zhouzifei.mediaTool.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * 删除文件PO
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@ApiModel(value = "删除文件PO")
public class DeletePO implements Serializable {

    private static final long serialVersionUID = -1604961110716421918L;

    /**
     * 要删除的文件记录ID，多个用__,__隔开
     */
    @ApiModelProperty(value = "要删除的文件记录ID，多个用__,__隔开", required = true)
    @NotBlank(message = "文件id不能为空")
    private String fileIds;

    public DeletePO() {
    }

    public String getFileIds() {
        return fileIds;
    }

    public void setFileIds(String fileIds) {
        this.fileIds = fileIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletePO deletePO = (DeletePO) o;
        return Objects.equals(fileIds, deletePO.fileIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileIds);
    }

    @Override
    public String toString() {
        return "DeletePO{" +
                "fileIds='" + fileIds + '\'' +
                '}';
    }

}
