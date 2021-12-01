package com.zhouzifei.simplefile.modules.dao;

import com.zhouzifei.simplefile.modules.vo.RPanUserFileVO;

import java.util.List;

/**
 * @author 周子斐
 * @date 2021/11/30
 * @Description
 */
public interface RPanUserFileRepository {

    List<RPanUserFileVO> selectRPanUserFileVOListByUserIdAndFileTypeAndParentIdAndDelFlag(Long userId,List<Integer> fileTypeArray,Long parentId,Integer delFlag);

}
