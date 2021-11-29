package com.zhouzifei.simplefile.modules.user.dao;

import com.zhouzifei.simplefile.modules.share.vo.ShareUserInfoVO;
import com.zhouzifei.simplefile.modules.user.entity.RPanUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 用户信息数据层
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Repository(value = "rPanUserMapper")
public interface RPanUserMapper {

    int deleteByPrimaryKey(Long userId);

    int insert(RPanUser record);

    int insertSelective(RPanUser record);

    RPanUser selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(RPanUser record);

    int updateByPrimaryKey(RPanUser record);

    String selectQuestionByUsername(@Param("username") String username);

    int selectCountByUsernameAndQuestionAndAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    RPanUser selectByUserId(@Param("userId") String userId);

    ShareUserInfoVO selectShareUserInfoVOByUserId(@Param("userId") Long userId);

    RPanUser selectByUsername(@Param("username") String username);

}