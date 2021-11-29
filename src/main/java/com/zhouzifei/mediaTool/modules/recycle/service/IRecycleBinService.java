package com.zhouzifei.mediaTool.modules.recycle.service;

import com.zhouzifei.mediaTool.modules.file.vo.RPanUserFileVO;

import java.util.List;

/**
 * 回收站业务处理接口
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
public interface IRecycleBinService {

    List<RPanUserFileVO> list(Long userId);

    void restore(String fileIds, Long userId);

    void delete(String fileIds, Long userId);

}
