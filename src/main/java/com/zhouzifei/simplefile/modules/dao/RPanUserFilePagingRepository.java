package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.bo.FilePositionBO;
import com.zhouzifei.simplefile.modules.entity.RPanUserFile;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileSearchVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户文件数据层
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Repository(value = "rPanUserFilePagingRepository")
public interface RPanUserFilePagingRepository extends CrudRepository<RPanUserFile,Long> {


    @Query("SELECT * FROM r_pan_user_file WHERE user_id = :userId AND file_id = :fileId AND del_flag = 0")
    RPanUserFile selectByFileIdAndUserId(@Param("fileId") Long fileId, @Param("userId") Long userId);

    @Query("SELECT COUNT(1) FROM r_pan_user_file WHERE user_id = :userId AND del_flag = 0 AND parent_id = :parentId AND filename = :filename")
    int selectCountByUserIdAndFilenameAndParentId(@Param("userId") Long userId, @Param("filename") String filename, @Param("parentId") Long parentId);

    @Query("SELECT* FROM r_pan_user_file WHERE user_id = :userId AND del_flag = 0 AND folder_flag = 1")
    List<RPanUserFile> selectFolderListByUserId(@Param("userId") Long userId);

    @Query(" SELECT * FROM r_pan_user_file WHERE file_id IN :idList")
    List<RPanUserFile> selectListByFileIdList(@Param("idList") List<Long> idList);

    @Query("SELECT file_id,parent_id,filename,folder_flag,file_size_desc,file_type,create_time,update_time FROM " +
            "            r_pan_user_file WHERE user_id = :userId AND del_flag = 0 AND filename LIKE %:keyword% AND file_type IN :fileTypeArray")
    List<RPanUserFileSearchVO> selectRPanUserFileVOListByUserIdAndFilenameAndFileTypes(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("fileTypeArray") List<Integer> fileTypeArray);

    @Query("SELECT file_id ,parent_id,filename,folder_flag,file_size_desc,file_type,create_time,update_time FROM r_pan_user_file\n" +
            "        WHERE file_id = :fileId AND user_id = :userId AND del_flag = 0 ")
    RPanUserFileVO selectRPanUserFileVOByFileIdAndUserId(@Param("fileId") Long fileId, @Param("userId") Long userId);

    @Query("SELECT * FROM r_pan_user_file WHERE  parent_id = :parentId")
    List<RPanUserFile> selectAllListByParentId(@Param("parentId") Long parentId);

    @Query("SELECT * FROM r_pan_user_file WHERE parent_id = :parentId AND del_flag = 0")
    List<RPanUserFile> selectAvailableListByParentId(@Param("parentId") Long parentId);

    @Query("SELECT file_id,parent_id,filename,folder_flag,file_size_desc,file_type,create_time,update_time \n" +
            "        FROM r_pan_user_file WHERE parent_id = :parentId AND del_flag = 0 ")
    List<RPanUserFileVO> selectAvailableRPanUserFileVOListByParentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(1) FROM  r_pan_user_file WHERE real_file_id = :realFileId")
    int selectCountByRealFileId(@Param("realFileId") Long realFileId);

    @Query("SELECT file_id,parent_id,filename,folder_flag,file_size_desc,file_type,create_time,update_time r_pan_user_file WHERE del_flag = 0 AND file_id IN :fileIdList ")
    List<RPanUserFileVO> selectRPanUserFileVOListByFileIdList(@Param("fileIdList") List<Long> fileIdList);

    @Query("SELECT * FROM r_pan_user_file WHERE user_id = :userId AND del_flag = 0 AND parent_id = 0 AND folder_flag = 1")
    RPanUserFile selectTopFolderByUserId(@Param("userId") Long userId);

    @Query("SELECT file_id FROM r_pan_user_file WHERE parent_id = :parentId AND del_flag = 0")
    List<Long> selectAvailableFileIdListByParentId(@Param("parentId") Long parentId);

    @Query("SELECT file_id,filename  FROM r_pan_user_file WHERE del_flag = 0 AND file_id IN :fileIdList ")
    List<FilePositionBO> selectFilePositionBOListByFileIds(@Param("fileIdList") List<Long> fileIdList);

}