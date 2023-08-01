package com.free.fs.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.free.fs.constant.CommonConstant;
import com.free.fs.exception.BusinessException;
import com.free.fs.model.Menu;
import com.free.fs.utils.FileUtil;
import com.free.fs.utils.R;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        final FileUploader fileUploader = FileUploader.builder().simpleFsProperties(simpleFsProperties).storageType(fileType).build();
        final ApiClient apiClient = fileUploader.execute();
        final FileListRequesr fileListRequesr = new FileListRequesr();
        fileListRequesr.setFold(pojo.getDirIds());
        return apiClient.fileList(fileListRequesr);
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
        final FileUploader fileUploader = FileUploader.builder().simpleFsProperties(simpleFsProperties).storageType(fileType).build();
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
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateByName(FilePojo pojo) {
        if (pojo.getName().equals(pojo.getRename())) {
            throw new BusinessException("当前名称与原始名称相同，请修改后重试！");
        }
//        FilePojo p = fileMapper.selectById(pojo.getId());
//        Integer count = fileMapper.selectCount(
//                new LambdaQueryWrapper<FilePojo>()
//                        .eq(FilePojo::getName, pojo.getRename())
//                        .eq(FilePojo::getIsDir,p.getIsDir())
//                        .eq(FilePojo::getParentId,p.getParentId())
//        );
//        if (count > 0) {
//            throw new BusinessException("当前目录已存在该名称,请修改后重试！");
//        }
//        FilePojo updPojo = new FilePojo();
//        updPojo.setId(pojo.getId());
//        updPojo.setName(pojo.getRename());
        return true;
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
//            if (fileMapper.updateById(updatePojo) <= 0) {
//                throw new BusinessException("移动失败");
//            }
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
    public Menu getTypeInfo(String type) {
        log.info("查询存储为{}的信息", type);
        List<Menu> menus;
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("json/menu.json")) {
            final String parseInputStream = com.zhouzifei.tool.util.FileUtil.parseInputStream(inputStream);
            menus = JSONObject.parseArray(parseInputStream, Menu.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Map<String, Menu> collect = menus.stream().collect(Collectors.toMap(Menu::getSource, menu -> menu));
        final Menu menu = collect.get(type);
        log.info("查询存储为{}的信息为{}", type, menu);
        return menu;
    }

    @Override
    public void saveType(Menu newMenu) {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("json/menu.json")) {
            final String parseInputStream = com.zhouzifei.tool.util.FileUtil.parseInputStream(inputStream);
            List<Menu> menus = JSONObject.parseArray(parseInputStream, Menu.class);
            final Map<String, Menu> collect = menus.stream().collect(Collectors.toMap(Menu::getSource, menu1 -> menu1));
            final Menu menu = collect.get(newMenu.getSource());
            menu.setConfig(newMenu.getConfig());
            collect.put(newMenu.getSource(), newMenu);
            final Collection<Menu> values = collect.values();
            List<Menu> list = new ArrayList<>(Arrays.asList(values.toArray(new Menu[0])));
            final String jsonString = JSONArray.toJSONString(list);
            File file = ResourceUtils.getFile("classpath:xiaozi.txt");
            com.zhouzifei.tool.util.FileUtil.writeByteArrayToFile(file, jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
