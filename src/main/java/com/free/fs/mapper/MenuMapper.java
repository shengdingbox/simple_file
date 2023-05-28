package com.free.fs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.free.fs.model.Menu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表mapper接口
 *
 * @author dinghao
 * @date 2021/3/12
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
}
