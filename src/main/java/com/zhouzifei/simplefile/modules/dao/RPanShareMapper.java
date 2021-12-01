package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.entity.RPanShare;
import com.zhouzifei.simplefile.modules.vo.RPanUserShareUrlVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分享数据层
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Repository(value = "rPanShareMapper")
public interface RPanShareMapper {

    int deleteByPrimaryKey(Long shareId);

    int insert(RPanShare record);

    int insertSelective(RPanShare record);

    RPanShare selectByPrimaryKey(Long shareId);

    int updateByPrimaryKeySelective(RPanShare record);

    int updateByPrimaryKey(RPanShare record);

    List<RPanUserShareUrlVO> selectRPanUserShareUrlVOListByUserId(@Param("userId") Long userId);

    int deleteByShareIdListAndUserId(@Param("shareIdList") List<Long> shareIdList, @Param("userId") Long userId);

    RPanShare selectByShareId(@Param("shareId") String shareId);

    int changeShareStatusByShareId(@Param("shareId") Long shareId, @Param("shareStatus") Integer shareStatus);

}