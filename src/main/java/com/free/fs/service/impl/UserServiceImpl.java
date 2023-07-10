package com.free.fs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.free.fs.constant.CommonConstant;
import com.free.fs.exception.BusinessException;
import com.free.fs.dto.MenuDTO;
import com.free.fs.mapper.MenuMapper;
import com.free.fs.mapper.RoleMapper;
import com.free.fs.mapper.UserMapper;
import com.free.fs.mapper.UserRoleMapper;
import com.free.fs.model.*;
import com.free.fs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.rmi.MarshalException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务接口实现
 *
 * @author dinghao
 * @date 2021/3/16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;

    private final RoleMapper roleMapper;

    private final MenuMapper menuMapper;


    @Override
    public List<MenuDTO>  getMenu() {
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>());
        List<MenuDTO> roots = menus.stream().filter(e -> {
            log.info("{}", e);
            return StringUtils.equals(e.getParentId(), "0");
        }).map(MenuDTO::of).collect(Collectors.toList());
        for (MenuDTO menuDTO : roots) {
            setChildren(menuDTO, menus);
        }
        return roots;
    }

    private static void setChildren(MenuDTO regionDTO, List<Menu> regionList) {
        Integer id = regionDTO.getId();
        List<MenuDTO> children = findChildren(String.valueOf(id), regionList);
        regionDTO.setChildren(children);
        for (MenuDTO child : children) {
            setChildren(child, regionList);
        }
        return;
    }

    private static List<MenuDTO> findChildren(String orgNo, List<Menu> regionDTOList) {
        return regionDTOList.stream().filter(e -> {
            return StringUtils.equals(orgNo, e.getParentId());
        }).map(MenuDTO::of).collect(Collectors.toList());
    }
}
