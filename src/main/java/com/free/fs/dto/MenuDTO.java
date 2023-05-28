package com.free.fs.dto;

import com.free.fs.model.Menu;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class MenuDTO {
    /**
     * 自增id
     */
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
    private List<MenuDTO> children;


    public static MenuDTO  of(Menu menu) {
        MenuDTO dto = new MenuDTO();
        BeanUtils.copyProperties(menu, dto);
        return dto;
    }
}
