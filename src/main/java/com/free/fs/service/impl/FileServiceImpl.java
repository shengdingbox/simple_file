package com.free.fs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.free.fs.common.constant.CommonConstant;
import com.free.fs.common.exception.BusinessException;
import com.free.fs.mapper.TypeMapper;
import com.free.fs.model.FileType;
import com.free.fs.utils.FileUtil;
import com.free.fs.utils.R;
import com.free.fs.mapper.FileMapper;
import com.free.fs.model.Dtree;
import com.free.fs.model.FilePojo;
import com.free.fs.service.FileService;
import com.zhouzifei.tool.config.SimpleFsProperties;
import com.zhouzifei.tool.dto.VirtualFile;
import com.zhouzifei.tool.entity.FileListRequesr;
import com.zhouzifei.tool.service.ApiClient;
import com.zhouzifei.tool.service.FileUploader;
import com.zhouzifei.tool.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件管理抽象接口实现
 *
 * @author dinghao
 * @date 2021/4/6
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    SimpleFsProperties simpleFsProperties;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    TypeMapper typeMapper;

    @SuppressWarnings("unchecked")
    @Override
    public List<VirtualFile> getList(FilePojo pojo, String fileType) {
        final FileUploader fileUploader = FileUploader.builder()
                .simpleFsProperties(simpleFsProperties)
                .storageType(fileType)
                .build();
        final ApiClient apiClient = fileUploader.execute();
        final FileListRequesr fileListRequesr = new FileListRequesr();
        fileListRequesr.setFold(pojo.getDirIds());
        return apiClient.fileList(fileListRequesr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Dtree> getTreeList(FilePojo pojo) {
        List<FilePojo> filePojos = fileMapper.selectList(
                new LambdaQueryWrapper<FilePojo>()
                        .eq(pojo.getIsDir()!=null && pojo.getIsDir(), FilePojo::getIsDir, pojo.getIsDir())
                        .orderByDesc(FilePojo::getIsDir, FilePojo::getPutTime)
        );
        List<Dtree> dtrees = new ArrayList<>();
        filePojos.forEach(item -> {
            Dtree dtree = new Dtree();
            BeanUtils.copyProperties(item,dtree);
            dtree.setTitle(item.getName());
            //设置图标
            if (dtree.getIsDir()) {
                dtree.setIconClass(CommonConstant.DTREE_ICON_1);
            } else {
                dtree.setIconClass(CommonConstant.DTREE_ICON_2);
            }
            dtrees.add(dtree);
        });
        return null;
    }

    @Override
    public List<Dtree> getDirTreeList(FilePojo pojo) {
        pojo.setIsDir(Boolean.TRUE);
        return this.getTreeList(pojo);
    }

    /**
     * 递归查询子节点
     *
     * @param root 根节点
     * @param all  所有节点
     * @return 根节点信息
     */

    private List<Dtree> getChildrens(Dtree root, List<Dtree> all) {
        return all.stream()
                .filter(m -> Objects.equals(m.getParentId(), root.getId()))
                .peek((m) -> m.setChildren(getChildrens(m, all)))
                .collect(Collectors.toList());
    }

    @Override
    public R upload(MultipartFile[] files, String dirIds, String fileType) {
        if (files == null || files.length == 0) {
            throw new BusinessException("文件不能为空");
        }
        for (MultipartFile file : files) {
            FilePojo filePojo = uploadFile(file, fileType);
            if (filePojo == null) {
                return R.failed("文件：" + file.getOriginalFilename() + "上传失败");
            }
        }
        return R.succeed("上传成功");
    }

    /**
     * 上传文件
     *
     * @param file
     * @param fileType
     */
    protected FilePojo uploadFile(MultipartFile file, String fileType) {
        final FileUploader fileUploader = FileUploader.builder()
                .simpleFsProperties(simpleFsProperties)
                .storageType(fileType)
                .build();
        final ApiClient apiClient = fileUploader.execute();
        final VirtualFile virtualFile = apiClient.uploadFile(file);
        log.debug(virtualFile.toString());
        final FilePojo filePojo = FileUtil.buildFilePojo(file, virtualFile.getOriginalFileName());
        filePojo.setUrl(virtualFile.getFullFilePath());
        filePojo.setSource(fileType);
        return filePojo;
    }


    @Override
    public R uploadSharding(MultipartFile[] files, String dirIds, HttpSession session) {
        if (files == null || files.length == 0) {
            throw new BusinessException("文件不能为空");
        }
        for (MultipartFile file : files) {
            FilePojo filePojo = uploadFileSharding(file, session);
            if (filePojo == null) {
                return R.failed("文件：" + file.getOriginalFilename() + "上传失败");
            }
        }
        return R.succeed("上传成功");
    }

    /**
     * 分片上传文件
     *
     * @param file
     * @param session
     */
    protected FilePojo uploadFileSharding(MultipartFile file, HttpSession session) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(String url) {
        //在删除七牛云
        deleteFile(url);
        return true;
    }

    @Override
    public void download(String url, HttpServletResponse response) {

    }

    /**
     * 删除文件资源
     *
     * @param url 文件路径
     */
    protected void deleteFile(String url) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addFolder(FilePojo pojo) {
        String dirId = pojo.getDirIds().substring(pojo.getDirIds().lastIndexOf(CommonConstant.DIR_SPLIT) + 1);
        if (0 > 0) {
            throw new BusinessException("当前目录名称已存在，请修改后重试！");
        }
        return fileMapper.insert(pojo) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateByName(FilePojo pojo) {
        if (pojo.getName().equals(pojo.getRename())) {
            throw new BusinessException("当前名称与原始名称相同，请修改后重试！");
        }
        FilePojo p = fileMapper.selectById(pojo.getId());
        Integer count = fileMapper.selectCount(
                new LambdaQueryWrapper<FilePojo>()
                        .eq(FilePojo::getName, pojo.getRename())
                        .eq(FilePojo::getIsDir,p.getIsDir())
                        .eq(FilePojo::getParentId,p.getParentId())
        );
        if (count > 0) {
            throw new BusinessException("当前目录已存在该名称,请修改后重试！");
        }
        FilePojo updPojo = new FilePojo();
        updPojo.setId(pojo.getId());
        updPojo.setName(pojo.getRename());
        return fileMapper.updateById(updPojo) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean move(String ids, Long parentId) {
        if (StringUtils.isEmpty(ids)) {
            throw new BusinessException("请选择要移动的文件或目录");
        }
        String[] idsArry = ids.split(CommonConstant.STRING_SPLIT);
        FilePojo updatePojo;
        for (String id : idsArry) {
            updatePojo = new FilePojo();
            updatePojo.setId(Long.parseLong(id));
            updatePojo.setParentId(parentId);
            if (fileMapper.updateById(updatePojo) <= 0) {
                throw new BusinessException("移动失败");
            }
        }
        return true;
    }


    @Override
    public Map<String, Object> getDirs(Long id) {
        Map<String, Object> map = new HashMap<>();
//        map.put("dirs", dir.length() > 0 ? dir.deleteCharAt(dir.length() - 1).toString() : "");
//        map.put("dirIds", dirIds.length() > 0 ? dirIds.deleteCharAt(dirIds.length() - 1).toString() : "");
        return map;
    }

    @Override
    public List<Dtree> getFileType(FilePojo pojo) {
        return null;
    }

    @Override
    public FileType getTypeInfo(String type) {
        log.info("查询存储为{}的信息",type);
        final FileType fileType = typeMapper.selectOne(new LambdaQueryWrapper<FileType>()
                .eq(FileType::getStorageType, type));
        log.info("查询存储为{}的信息为{}",type,fileType);
        return fileType;
    }
}
