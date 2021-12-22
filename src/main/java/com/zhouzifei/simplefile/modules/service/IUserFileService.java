package com.zhouzifei.simplefile.modules.service;

import com.zhouzifei.simplefile.modules.entity.RPanUserFile;
import com.zhouzifei.simplefile.modules.vo.BreadcrumbVO;
import com.zhouzifei.simplefile.modules.vo.FolderTreeNodeVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileSearchVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户文件业务处理接口
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
public interface IUserFileService {

    List<RPanUserFileVO> list(Long parentId, String fileTypes, Long userId);

    List<RPanUserFileVO> list(Long parentId, String fileTypes, Long userId, Integer delFlag);

    List<RPanUserFileVO> list(String fileIds);

    void createFolder(Long parentId, String folderName, Long userId);

    void updateFilename(Long fileId, String newFilename, Long userId);

    void delete(String fileIds, Long userId);

    void upload(MultipartFile file, Long parentId, Long userId, String md5, Long size,String storageType);

    void uploadWithChunk(MultipartFile file, Long parentId, Long userId, String md5, Integer chunks, Integer chunk, Long size, String name,String storageType);

    void download(Long fileId, HttpServletResponse response, Long userId);

    void download(Long fileId, HttpServletResponse response,String storageType);

    List<FolderTreeNodeVO> getFolderTree(Long userId);

    void transfer(String fileIds, Long targetParentId, Long userId);

    void copy(String fileIds, Long targetParentId, Long userId);

    List<RPanUserFileSearchVO> search(String keyword, String fileTypes, Long userId);

    RPanUserFileVO detail(Long fileId, Long userId);

    List<BreadcrumbVO> getBreadcrumbs(Long fileId, Long userId);

    void preview(Long fileId, HttpServletResponse response, Long userId);

    void restoreUserFiles(String fileIds, Long userId);

    void physicalDeleteUserFiles(String fileIds, Long userId,String storageType);

    List<RPanUserFileVO> allList(String fileIds);

    RPanUserFile getUserTopFileInfo(Long userId);

    String getAllAvailableFileIdByFileIds(String fileIds);

    boolean checkAllUpFileAvailable(List<Long> fileIds);

    boolean secUpload(Long parentId, String filename, String md5, Long userId);

}
