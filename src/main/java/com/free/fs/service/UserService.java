package com.free.fs.service;

import com.free.fs.model.User;

/**
 * 用户表业务接口
 *
 * @author dinghao
 * @date 2021/3/16
 */
public interface UserService{

    /**
     * 新增用户
     *
     * @param user
     * @return
     */
    boolean addUser(User user);
}
