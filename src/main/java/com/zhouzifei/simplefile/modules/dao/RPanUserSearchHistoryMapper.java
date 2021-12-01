package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.entity.RPanUserSearchHistory;
import com.zhouzifei.simplefile.modules.vo.RPanUserSearchHistoryVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户搜索历史数据层
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Repository(value = "rPanUserSearchHistoryMapper")
public interface RPanUserSearchHistoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(RPanUserSearchHistory record);

    int insertSelective(RPanUserSearchHistory record);

    RPanUserSearchHistory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RPanUserSearchHistory record);

    int updateByPrimaryKey(RPanUserSearchHistory record);

    List<RPanUserSearchHistoryVO> selectRPanUserSearchHistoryVOListByUserId(@Param("userId") Long userId);

    int updateUpdateTimeBySearchContentAndUserId(@Param("searchContent") String searchContent, @Param("userId") Long userId);

}