package com.free.fs.service;

import com.free.fs.utils.R;
import com.free.fs.model.Dtree;
import com.free.fs.model.FilePojo;
import com.zhouzifei.tool.dto.VirtualFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 文件管理接口
 *
 * @author dinghao
 * @date 2021/3/15
 */
public interface FileService{

    /**
     * 获取文件列表
     *
     * @param pojo
     * @return
     */
    public List<VirtualFile> getList(FilePojo pojo, String fileType);

    /**
     * 获取文件树结构列表
     *
     * @param pojo
     * @return
     */
    List<Dtree> getTreeList(FilePojo pojo);

    /**
     * 获取目录树结构列表
     *
     * @param pojo
     * @return
     */
    List<Dtree> getDirTreeList(FilePojo pojo);

    /**
     * 上传文件
     *
     * @param files
     * @param dirIds
     * @param fileType
     * @return
     */
    R upload(MultipartFile[] files, String dirIds, String fileType);

    /**
     * 分片上传大文件
     *
     * @param files
     * @return
     */
    R uploadSharding(MultipartFile[] files,String dirIds, HttpSession session);

    /**
     * 删除文件
     *
     * @param url
     * @return
     */
    boolean delete(String url);

    /**
     * 下载文件
     *
     * @param url
     * @return
     */
    void download(String url, HttpServletResponse response);

    /**
     * 新增文件夹
     *
     * @param pojo
     * @return
     */
    boolean addFolder(FilePojo pojo);

    /**
     * 重命名
     *
     * @param pojo
     * @return
     */
    boolean updateByName(FilePojo pojo);

    /**
     * 移动文件
     *
     * @param ids
     * @param parentId
     * @return
     */
    boolean move(String ids, Long parentId);

    /**
     * 根据id拼接父目录
     *
     * @param id
     * @return
     */
    Map<String, Object> getDirs(Long id);

    List<Dtree> getFileType(FilePojo pojo);
    List<Dtree> getTypeInfo(String type);
}
