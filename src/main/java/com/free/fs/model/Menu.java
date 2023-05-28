package com.free.fs.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_menu")
@EqualsAndHashCode(callSuper = true)
public class Menu extends Model<Menu> {
    /**
     * 自增id
     */
    @TableId
    private Integer id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单地址
     */
    private String url;
    /**、
     * 父级id
     */
    private String parentId;
}
