package com.zhouzifei.simplefile.modules.service.impl;

import com.google.common.base.Splitter;
import com.zhouzifei.simplefile.common.constant.CommonConstant;
import com.zhouzifei.simplefile.common.exception.RPanException;
import com.zhouzifei.simplefile.modules.dao.RPanFileRepository;
import com.zhouzifei.simplefile.modules.entity.RPanFile;
import com.zhouzifei.simplefile.modules.service.IFileService;
import com.zhouzifei.simplefile.util.FileUtil;
import com.zhouzifei.simplefile.util.IdGenerator;
import com.zhouzifei.tool.config.FileProperties;
import com.zhouzifei.tool.consts.StorageTypeConst;
import com.zhouzifei.tool.dto.VirtualFile;
import com.zhouzifei.tool.entity.MetaDataRequest;
import com.zhouzifei.tool.service.ApiClient;
import com.zhouzifei.tool.service.FileUploader;
import com.zhouzifei.tool.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 物理文件业务处理类
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Service(value = "fileService")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class FileServiceImpl implements IFileService {

    @Autowired
    RPanFileRepository rPanFileRepository;

    @Autowired
    private FileProperties fileProperties;


    /**
     * 保存物理文件
     *
     * @param file
     * @param userId
     * @param md5
     * @param size
     * @return
     */
    @Override
    public RPanFile save(MultipartFile file, Long userId, String md5, Long size,String storageType) {
        RPanFile rPanFile = assembleRPanFile(uploadFile(file,storageType), userId, md5, size, file.getOriginalFilename());
        saveFileInfo(rPanFile);
        return rPanFile;
    }

    /**
     * 保存分片文件
     *
     * @param file
     * @param userId
     * @param md5
     * @param chunks
     * @param chunk
     * @param size
     * @param name
     * @return
     */
    @Override
    public RPanFile saveWithChunk(MultipartFile file, Long userId, String md5, Integer chunks, Integer chunk, Long size, String name,String storageType) {
        String filePath = uploadFileWithChunk(file, md5, chunks, chunk, size, name,storageType);
        if (StringUtils.isNullOrEmpty(filePath)) {
            RPanFile rPanFile = assembleRPanFile(filePath, userId, md5, size, name);
            saveFileInfo(rPanFile);
            return rPanFile;
        }
        return null;
    }

    /**
     * 删除物理文件
     *
     * @param fileIds
     * @return
     */
    @Override
    public void delete(String fileIds,String storageType) {
        if (StringUtils.isBlank(fileIds)) {
            throw new RPanException("文件id不能为空");
        }
        // TODO 集成MQ 优化成异步消息操作 加上重试机制
        deletePhysicalFiles(fileIds,storageType);
        deleteFileInfos(fileIds);
    }

    /**
     * 获取实体文件详情
     *
     * @param realFileId
     * @return
     */
    @Override
    public RPanFile getFileDetail(Long realFileId) {
        final RPanFile rPanFile = rPanFileRepository.findById(realFileId).orElseThrow(RuntimeException::new);
        return rPanFile;
    }

    @Override
    public List<RPanFile> getFileListByMd5(String md5) {
        return rPanFileRepository.selectByMd5(md5);
    }

    /**
     * 保存物理文件信息
     *
     * @param rPanFile
     */
    private void saveFileInfo(RPanFile rPanFile) {
        rPanFileRepository.save(rPanFile);
    }

    /**
     * 上传物理文件
     *
     * @param file
     */
    private String uploadFile(MultipartFile file, String storageType) {
        final FileUploader fileUploader = new FileUploader();
        final StorageTypeConst enumType = StorageTypeConst.getEnumType(storageType);
        final ApiClient apiClient = fileUploader.getApiClient(enumType, fileProperties);
        try {
            final VirtualFile virtualFile = apiClient.uploadFile(file.getInputStream(), file.getOriginalFilename());
            return virtualFile.getFullFilePath();
        } catch (IOException e) {
            log.error("上传失败", e);
            throw new RPanException("上传失败");
        }
    }

    /**
     * 拼装物理文件信息
     *
     * @param filePath
     * @param userId
     * @param md5
     * @param size
     * @param name
     * @return
     */
    private RPanFile assembleRPanFile(String filePath, Long userId, String md5, Long size, String name) {
        RPanFile rPanFile = new RPanFile();
        String suffix = FileUtil.getFileSuffix(name);
        String newFileName = FileUtil.getFilename(filePath);
        rPanFile.setFileId(IdGenerator.nextId());
        rPanFile.setFilename(newFileName);
        rPanFile.setRealPath(filePath);
        rPanFile.setFileSize(String.valueOf(size));
        rPanFile.setFileSizeDesc(FileUtil.getFileSizeDesc(size));
        rPanFile.setFileSuffix(suffix);
        rPanFile.setFilePreviewContentType(FileUtil.getContentType(filePath));
        rPanFile.setMd5(md5);
        rPanFile.setCreateUser(userId);
        rPanFile.setCreateTime(new Date());
        return rPanFile;
    }

    /**
     * 删除物理文件信息
     *
     * @param fileIds
     * @return
     */
    private void deleteFileInfos(String fileIds) {
        List<String> fileIdList = Splitter.on(CommonConstant.COMMON_SEPARATOR).splitToList(fileIds);
        for (String s : fileIdList) {
            rPanFileRepository.deleteById(Long.getLong(s));
        }
    }

    /**
     * 批量删除物理文件
     *
     * @param fileIds
     * @return
     */
    private void deletePhysicalFiles(String fileIds,String storageType) {
        final FileUploader fileUploader = new FileUploader();
        final StorageTypeConst enumType = StorageTypeConst.getEnumType(storageType);
        final ApiClient apiClient = fileUploader.getApiClient(enumType, fileProperties);
        final List<String> strings = Splitter.on(CommonConstant.COMMON_SEPARATOR).splitToList(fileIds);
        List<RPanFile> rPanFileList = new ArrayList<>();
        for (String string : strings) {
            final RPanFile rPanFile = rPanFileRepository.findById(Long.getLong(string)).orElseGet(RPanFile::new);
            rPanFileList.add(rPanFile);
        }
        rPanFileList.stream().map(RPanFile::getRealPath).forEach(apiClient::removeFile);
    }

    /**
     * 上传分片文件
     *
     * @param file
     * @param md5
     * @param chunks
     * @param chunk
     * @param size
     * @param name
     * @return 分片文件是否都上传完毕
     */
    private String uploadFileWithChunk(MultipartFile file, String md5, Integer chunks, Integer chunk, Long size, String name,String storageType) {
        final FileUploader fileUploader = new FileUploader();
        final StorageTypeConst enumType = StorageTypeConst.getEnumType(storageType);
        final ApiClient apiClient = fileUploader.getApiClient(enumType, fileProperties);
        MetaDataRequest metaDataRequest = new MetaDataRequest();
        metaDataRequest.setFileMd5(md5);
        metaDataRequest.setChunks(String.valueOf(chunks));
        metaDataRequest.setChunk(String.valueOf(chunk));
        metaDataRequest.setChunkSize(Math.toIntExact(size));
        metaDataRequest.setSize(file.getSize());
        //metaDataRequest.setName(name);
        VirtualFile virtualFile = apiClient.multipartUpload(file, metaDataRequest);
        return virtualFile.getFullFilePath();
    }

}

