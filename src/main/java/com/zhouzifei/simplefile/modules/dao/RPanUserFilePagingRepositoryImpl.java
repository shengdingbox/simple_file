package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.vo.RPanUserFileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 周子斐
 * @date 2021/11/30
 * @Description
 */
@Slf4j
@Repository(value = "rPanUserFileRepository")
@Transactional
public class RPanUserFilePagingRepositoryImpl implements RPanUserFileRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    private BeanPropertyRowMapper<RPanUserFileVO> rowMapper = BeanPropertyRowMapper.newInstance(RPanUserFileVO.class);

    @Override
    public List<RPanUserFileVO> selectRPanUserFileVOListByUserIdAndFileTypeAndParentIdAndDelFlag(Long userId, List<Integer> fileTypeArray, Long parentId, Integer delFlag) {
        StringBuilder sql = new StringBuilder("SELECT file_id AS fileId,parent_id AS parentId,filename AS filename,folder_flag AS folderFlag,file_size_desc AS fileSizeDesc,\n" +
                "            file_type AS fileType,create_time AS createTime,update_time AS updateTime FROM r_pan_user_file\n" +
                "        WHERE 1=1");
        Map<String, Object> params = new HashMap<>();
        if (null != userId) {
            sql.append(" AND user_id = :userId ");
            params.put("userId", userId);
        }

        if (!CollectionUtils.isEmpty(fileTypeArray)) {
            sql.append("  AND file_type IN :fileTypeArray ");
            params.put("fileTypeArray", String.join(",", (CharSequence) fileTypeArray));
        }
        if (null != parentId) {
            sql.append(" AND parent_id = :parentId ");
            params.put("parentId", parentId);
        }
        if (null != delFlag) {
            sql.append(" AND del_flag = :delFlag ");
            params.put("delFlag", delFlag);
        }
        log.info("商户管理列表持久层sql为:{}", sql.toString());
        return jdbc.query(sql.toString(), params, rowMapper);
    }
}
