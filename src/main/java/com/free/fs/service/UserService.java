package com.free.fs.service;

import com.alibaba.fastjson.JSONObject;
import com.free.fs.dto.MenuDTO;
import com.free.fs.model.User;

import java.util.List;

/**
 * 用户表业务接口
 *
 * @author dinghao
 * @date 2021/3/16
 */
public interface UserService{

    List<MenuDTO> getMenu();
}
