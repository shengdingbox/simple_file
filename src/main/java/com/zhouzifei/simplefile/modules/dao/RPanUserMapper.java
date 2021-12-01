package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.vo.ShareUserInfoVO;
import com.zhouzifei.simplefile.modules.entity.RPanUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户信息数据层
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Repository(value = "rPanUserMapper")
public interface RPanUserMapper extends CrudRepository<RPanUser,Long> {

    int insert(RPanUser record);

    String selectQuestionByUsername(@Param("username") String username);

    int selectCountByUsernameAndQuestionAndAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    ShareUserInfoVO selectShareUserInfoVOByUserId(@Param("userId") Long userId);

    @Query("select * from r_pan_user where username = :username")
    RPanUser selectByUsername(@Param("username") String username);

}