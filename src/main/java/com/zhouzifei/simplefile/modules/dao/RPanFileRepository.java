package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.entity.RPanFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author 周子斐
 * @date 2021/11/30
 * @Description
 */
public interface RPanFileRepository extends CrudRepository<RPanFile,Long> {

    @Query("SELECT * FROM  r_pan_file WHERE md5 = :md5")
    List<RPanFile> selectByMd5(@Param("md5") String md5);
}
