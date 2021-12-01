package com.zhouzifei.simplefile.modules.service.impl;

import com.google.common.collect.Lists;
import com.zhouzifei.simplefile.common.constant.CommonConstant;
import com.zhouzifei.simplefile.common.exception.RPanException;
import com.zhouzifei.simplefile.common.response.ResponseCode;
import com.zhouzifei.simplefile.modules.bo.FilePositionBO;
import com.zhouzifei.simplefile.modules.constant.FileConstant;
import com.zhouzifei.simplefile.modules.dao.RPanUserFilePagingRepository;
import com.zhouzifei.simplefile.modules.dao.RPanUserFileRepository;
import com.zhouzifei.simplefile.modules.entity.RPanFile;
import com.zhouzifei.simplefile.modules.entity.RPanUserFile;
import com.zhouzifei.simplefile.modules.service.IFileService;
import com.zhouzifei.simplefile.modules.service.IUserFileService;
import com.zhouzifei.simplefile.modules.vo.BreadcrumbVO;
import com.zhouzifei.simplefile.modules.vo.FolderTreeNodeVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileSearchVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileVO;
import com.zhouzifei.simplefile.modules.service.IShareService;
import com.zhouzifei.simplefile.modules.service.IUserSearchHistoryService;
import com.zhouzifei.simplefile.util.FileUtil;
import com.zhouzifei.simplefile.util.HttpUtil;
import com.zhouzifei.simplefile.util.IdGenerator;
import com.zhouzifei.simplefile.util.StringListUtil;
import com.zhouzifei.simplefile.util.file.type.context.FileTypeContext;
import com.zhouzifei.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户文件业务处理实现
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@Service(value = "userFileService")
@Transactional(rollbackFor = Exception.class)
public class UserFileServiceImpl implements IUserFileService {

    private static final Logger log = LoggerFactory.getLogger(UserFileServiceImpl.class);

    @Autowired
    @Qualifier(value = "rPanUserFilePagingRepository")
    private RPanUserFilePagingRepository rPanUserFilePagingRepository;

    @Autowired
    @Qualifier(value = "fileService")
    private IFileService iFileService;

    @Autowired
    @Qualifier(value = "userSearchHistoryService")
    private IUserSearchHistoryService iUserSearchHistoryService;

    @Autowired
    @Qualifier(value = "shareService")
    private IShareService iShareService;

    @Autowired
    @Qualifier(value = "rPanUserFileRepository")
    RPanUserFileRepository rPanUserFileRepository;

    /**
     * 获取文件列表
     *
     * @param parentId
     * @param fileTypes
     * @param userId
     * @return
     */
    @Override
    public List<RPanUserFileVO> list(Long parentId, String fileTypes, Long userId) {
        return list(parentId, fileTypes, userId, FileConstant.DelFlagEnum.NO.getCode());
    }

    /**
     * 获取文件列表
     *
     * @param parentId
     * @param fileTypes
     * @param userId
     * @param delFlag
     * @return
     */
    @Override
    public List<RPanUserFileVO> list(Long parentId, String fileTypes, Long userId, Integer delFlag) {
        List<Integer> fileTypeArray = null;
        if (Objects.equals(CommonConstant.ZERO_LONG, parentId)) {
            return Lists.newArrayList();
        }
        if (!Objects.equals(fileTypes, FileConstant.ALL_FILE_TYPE)) {
            fileTypeArray = StringListUtil.string2IntegerList(fileTypes);
        }
        return rPanUserFileRepository.selectRPanUserFileVOListByUserIdAndFileTypeAndParentIdAndDelFlag(userId, fileTypeArray, parentId, delFlag);
    }

    /**
     * 获取文件列表
     *
     * @param fileIds
     * @return
     */
    @Override
    public List<RPanUserFileVO> list(String fileIds) {
        return rPanUserFilePagingRepository.selectRPanUserFileVOListByFileIdList(StringListUtil.string2LongList(fileIds));
    }

    /**
     * 创建文件夹
     *
     * @param parentId
     * @param folderName
     * @param userId
     */
    @Override
    public void createFolder(Long parentId, String folderName, Long userId) {
        saveUserFile(parentId, folderName, FileConstant.FolderFlagEnum.YES, null, null, userId, null);
    }

    /**
     * 文件重命名
     *
     * @param fileId
     * @param newFilename
     * @param userId
     */
    @Override
    public void updateFilename(Long fileId, String newFilename, Long userId) {
        RPanUserFile originalUserFileInfo = getRPanUserFileByFileIdAndUserId(fileId, userId);
        if (Objects.equals(originalUserFileInfo.getFilename(), newFilename)) {
            throw new RPanException("请使用一个新名称");
        }
        if (rPanUserFilePagingRepository.selectCountByUserIdAndFilenameAndParentId(userId, newFilename, originalUserFileInfo.getParentId()) > CommonConstant.ZERO_INT) {
            throw new RPanException("名称已被占用");
        }
        originalUserFileInfo.setFilename(newFilename);
        originalUserFileInfo.setUpdateUser(userId);
        originalUserFileInfo.setUpdateTime(new Date());
        rPanUserFilePagingRepository.save(originalUserFileInfo);
    }

    /**
     * 删除文件(批量)
     *
     * @param fileIds
     * @param userId
     */
    @Override
    public void delete(String fileIds, Long userId) {
        List<Long> idList = StringListUtil.string2LongList(fileIds);
        for (Long aLong : idList) {
            RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(aLong).orElseThrow(RuntimeException::new);
            rPanUserFile.setDelFlag(1);
            rPanUserFile.setUpdateUser(userId);
            rPanUserFile.setUpdateTime(new Date());
            rPanUserFilePagingRepository.save(rPanUserFile);
        }
        iShareService.refreshShareStatus(getAllAvailableFileIdByFileIds(fileIds));
    }

    /**
     * 文件上传
     *
     * @param file
     * @param parentId
     * @param md5
     * @param size
     */
    @Override
    public void upload(MultipartFile file, Long parentId, Long userId, String md5, Long size,String storageType) {
        RPanFile rPanFile = uploadRealFile(file, userId, md5, size,storageType);
        String filename = file.getOriginalFilename();
        saveUserFile(parentId, filename, FileConstant.FolderFlagEnum.NO, FileTypeContext.getFileTypeCode(filename), rPanFile.getFileId(), userId, rPanFile.getFileSizeDesc());
    }

    /**
     * 文件分片上传
     *
     * @param file
     * @param parentId
     * @param userId
     * @param md5
     * @param chunks
     * @param chunk
     * @param size
     * @param name
     * @return
     */
    @Override
    public void uploadWithChunk(MultipartFile file, Long parentId, Long userId, String md5, Integer chunks, Integer chunk, Long size, String name,String storageType) {
        RPanFile rPanFile = uploadRealFileWithChunk(file, userId, md5, chunks, chunk, size, name,storageType);
        if (rPanFile != null) {
            saveUserFile(parentId, name, FileConstant.FolderFlagEnum.NO, FileTypeContext.getFileTypeCode(name), rPanFile.getFileId(), userId, rPanFile.getFileSizeDesc());
        }
    }

    /**
     * 文件下载
     *
     * @param fileId
     * @param response
     * @param userId
     */
    @Override
    public void download(Long fileId, HttpServletResponse response, Long userId) {
        try {
            getRPanUserFileByFileIdAndUserId(fileId, userId);
        } catch (RPanException e) {
            throw new RPanException("您没有下载权限");
        }
        download(fileId, response);
    }

    /**
     * 文件下载
     *
     * @param fileId
     * @param response
     */
    @Override
    public void download(Long fileId, HttpServletResponse response) {
        if (checkIsFolder(fileId)) {
            throw new RPanException("不能选择文件夹下载");
        }
        RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(fileId).orElseThrow(RuntimeException::new);
        RPanFile rPanFile = iFileService.getFileDetail(rPanUserFile.getRealFileId());
        doDownload(rPanFile.getRealPath(), response, rPanUserFile.getFilename());
    }

    /**
     * 获取文件夹树
     *
     * @param userId
     * @return
     */
    @Override
    public List<FolderTreeNodeVO> getFolderTree(Long userId) {
        List<RPanUserFile> folderList = rPanUserFilePagingRepository.selectFolderListByUserId(userId);
        return assembleFolderTree(folderList);
    }

    /**
     * 批量转移文件
     *
     * @param fileIds
     * @param targetParentId
     * @param userId
     */
    @Override
    public void transfer(String fileIds, Long targetParentId, Long userId) {
        if (!checkIsFolder(targetParentId, userId)) {
            throw new RPanException("请选择要转移到的文件夹");
        }
        if (!checkTargetParentIdAvailable(targetParentId, fileIds)) {
            throw new RPanException("要转移的文件中包含选中的目标文件夹,请重新选择");
        }
        // 查询所有要被转移的文件信息
        List<RPanUserFile> toBeTransferredFileInfoList = rPanUserFilePagingRepository.selectListByFileIdList(StringListUtil.string2LongList(fileIds));
        toBeTransferredFileInfoList.stream().forEach(rPanUserFile -> transferOne(rPanUserFile, targetParentId));
    }

    /**
     * 批量复制文件
     *
     * @param fileIds
     * @param targetParentId
     * @param userId
     */
    @Override
    public void copy(String fileIds, Long targetParentId, Long userId) {
        if (!checkIsFolder(targetParentId, userId)) {
            throw new RPanException("请选择要复制到的文件夹");
        }
        if (!checkTargetParentIdAvailable(targetParentId, fileIds)) {
            throw new RPanException("要复制的文件中包含选中的目标文件夹,请重新选择");
        }
        doCopyUserFiles(fileIds, targetParentId, userId);
    }

    /**
     * 通过文件名搜索文件
     *
     * @param keyword
     * @param userId
     * @return
     */
    @Override
    public List<RPanUserFileSearchVO> search(String keyword, String fileTypes, Long userId) {
        saveUserSearchHistory(keyword, userId);
        List<Integer> fileTypeArray = null;
        if (!Objects.equals(fileTypes, FileConstant.ALL_FILE_TYPE)) {
            fileTypeArray = StringListUtil.string2IntegerList(fileTypes);
        }
        List<RPanUserFileSearchVO> rPanUserFileSearchVOList = rPanUserFilePagingRepository.selectRPanUserFileVOListByUserIdAndFilenameAndFileTypes(userId, keyword, fileTypeArray);
        if (!CollectionUtils.isEmpty(rPanUserFileSearchVOList)) {
            List<Long> parentIdList = rPanUserFileSearchVOList.stream().map(RPanUserFileSearchVO::getParentId).collect(Collectors.toList());
            List<FilePositionBO> filePositionBOList = rPanUserFilePagingRepository.selectFilePositionBOListByFileIds(parentIdList);
            final Map<Long, String> filePositionMap = filePositionBOList.stream().collect(Collectors.toMap(FilePositionBO::getFileId, FilePositionBO::getFilename));
            rPanUserFileSearchVOList.stream().forEach(rPanUserFileSearchVO -> rPanUserFileSearchVO.setParentFilename(filePositionMap.get(rPanUserFileSearchVO.getParentId())));
        }
        return rPanUserFileSearchVOList;
    }

    /**
     * 查询文件详情
     *
     * @param fileId
     * @param userId
     * @return
     */
    @Override
    public RPanUserFileVO detail(Long fileId, Long userId) {
        return rPanUserFilePagingRepository.selectRPanUserFileVOByFileIdAndUserId(fileId, userId);
    }


    /**
     * 获取面包屑列表
     *
     * @param fileId
     * @param userId
     * @return
     */
    @Override
    public List<BreadcrumbVO> getBreadcrumbs(Long fileId, Long userId) {
        List<RPanUserFile> rPanUserFileList = rPanUserFilePagingRepository.selectFolderListByUserId(userId);
        if (CollectionUtils.isEmpty(rPanUserFileList)) {
            return Lists.newArrayList();
        }
        Map<Long, BreadcrumbVO> breadcrumbVOTransferMap = rPanUserFileList.stream().collect(Collectors.toMap(RPanUserFile::getFileId, BreadcrumbVO::assembleBreadcrumbVO));
        List<BreadcrumbVO> breadcrumbVOList = Lists.newArrayList();
        BreadcrumbVO thisLevel = breadcrumbVOTransferMap.get(fileId);
        do {
            breadcrumbVOList.add(thisLevel);
            thisLevel = breadcrumbVOTransferMap.get(thisLevel.getParentId());
        } while (!Objects.isNull(thisLevel));
        Collections.reverse(breadcrumbVOList);
        return breadcrumbVOList;
    }

    /**
     * 预览单个文件
     *
     * @param fileId
     * @param userId
     * @param response
     */
    @Override
    public void preview(Long fileId, HttpServletResponse response, Long userId) {
        if (checkIsFolder(fileId, userId)) {
            throw new RPanException("不能预览文件夹");
        }
        RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(fileId).orElseThrow(RuntimeException::new);
        RPanFile fileDetail = iFileService.getFileDetail(rPanUserFile.getRealFileId());
        preview(fileDetail.getRealPath(), response, fileDetail.getFilePreviewContentType());
    }

    /**
     * 批量还原用户文件的删除状态
     *
     * @param fileIds
     * @param userId
     * @return
     */
    @Override
    public void restoreUserFiles(String fileIds, Long userId) {
        if (StringUtils.isBlank(fileIds) || Objects.isNull(userId)) {
            throw new RPanException("批量还原用户文件的删除状态失败");
        }
        List<Long> fileIdList = StringListUtil.string2LongList(fileIds);
        List<RPanUserFile> rPanUserFiles = rPanUserFilePagingRepository.selectListByFileIdList(fileIdList);
        Set<String> cleanSet = rPanUserFiles.stream().map(rPanUserFile -> rPanUserFile.getParentId() + rPanUserFile.getFilename()).collect(Collectors.toSet());
        if (cleanSet.size() != rPanUserFiles.size()) {
            throw new RPanException("文件还原失败，该还原文件中存在同名文件，请逐个还原并重命名");
        }
        for (RPanUserFile rPanUserFile : rPanUserFiles) {
            if (rPanUserFilePagingRepository.selectCountByUserIdAndFilenameAndParentId(rPanUserFile.getUserId(), rPanUserFile.getFilename(), rPanUserFile.getParentId()) != CommonConstant.ZERO_INT) {
                throw new RPanException("文件：" + rPanUserFile.getFilename() + "还原失败，该文件夹中已有相同名称的文件或文件夹，请重命名后重试");
            }
        }
        for (Long aLong : fileIdList) {
            final RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(aLong).orElseThrow(RuntimeException::new);
            rPanUserFile.setDelFlag(0);
            rPanUserFile.setUpdateUser(userId);
            rPanUserFile.setUpdateTime(new Date());
            rPanUserFilePagingRepository.save(rPanUserFile);
        }
    }

    /**
     * 物理删除用户文件
     *
     * @param fileIds
     * @param userId
     * @return
     */
    @Override
    public void physicalDeleteUserFiles(String fileIds, Long userId,String storageType) {
        if (StringUtils.isBlank(fileIds) || Objects.isNull(userId)) {
            throw new RPanException("物理删除用户文件失败");
        }
        List<RPanUserFile> rPanUserFileList = assembleAllToBeDeletedUserFileList(fileIds);
        List<Long> fileIdList = rPanUserFileList.stream().map(RPanUserFile::getFileId).collect(Collectors.toList());
        for (Long aLong : fileIdList) {
            RPanUserFile rPanUserFile = rPanUserFilePagingRepository.selectByFileIdAndUserId(aLong,userId);
            rPanUserFilePagingRepository.delete(rPanUserFile);
        }
        Set<Long> realFileIdSet = assembleAllUnusedRealFileIdSet(rPanUserFileList);
        if (CollectionUtils.isEmpty(realFileIdSet)) {
            return;
        }
        iFileService.delete(StringListUtil.longListToString(realFileIdSet),storageType);
    }

    /**
     * 获取对应文件列表的所有文件以及子文件信息
     *
     * @param fileIds
     * @return
     */
    @Override
    public List<RPanUserFileVO> allList(String fileIds) {
        if (StringUtils.isBlank(fileIds)) {
            throw new RPanException(ResponseCode.ERROR_PARAM);
        }
        List<RPanUserFileVO> rPanUserFileVOList = rPanUserFilePagingRepository.selectRPanUserFileVOListByFileIdList(StringListUtil.string2LongList(fileIds));
        if (CollectionUtils.isEmpty(rPanUserFileVOList)) {
            return Lists.newArrayList();
        }
        final List<RPanUserFileVO> allRPanUserFileVOList = Lists.newArrayList(rPanUserFileVOList);
        rPanUserFileVOList.stream().forEach(rPanUserFileVO -> findAllAvailableChildUserFile(allRPanUserFileVOList, rPanUserFileVO));
        return allRPanUserFileVOList;
    }

    /**
     * 获取用户的顶级文件信息
     *
     * @param userId
     * @return
     */
    @Override
    public RPanUserFile getUserTopFileInfo(Long userId) {
        return rPanUserFilePagingRepository.selectTopFolderByUserId(userId);
    }

    /**
     * 通过文件id集合获取所有文件id和子文件id
     *
     * @param fileIds
     * @return
     */
    @Override
    public String getAllAvailableFileIdByFileIds(String fileIds) {
        List<Long> fileIdList = StringListUtil.string2LongList(fileIds);
        List<Long> allAvailableFileId = Lists.newArrayList(fileIdList);
        fileIdList.forEach(fileId -> findAllAvailableFileIdByFileId(allAvailableFileId, fileId));
        return StringListUtil.longListToString(allAvailableFileId);
    }

    /**
     * 校验所有的父文件夹以及当前文件有效
     *
     * @param fileIds
     * @return
     */
    @Override
    public boolean checkAllUpFileAvailable(List<Long> fileIds) {
        for (Long fileId : fileIds) {
            if (!checkUpFileAvailable(fileId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 秒传文件
     *
     * @param parentId
     * @param filename
     * @param md5
     * @param userId
     * @return
     */
    @Override
    public boolean secUpload(Long parentId, String filename, String md5, Long userId) {
        List<RPanFile> rPanFileList = iFileService.getFileListByMd5(md5);
        if (CollectionUtils.isEmpty(rPanFileList) || rPanFileList.size() > CommonConstant.ONE_INT) {
            return false;
        }
        RPanFile rPanFile = rPanFileList.get(CommonConstant.ZERO_INT);
        saveUserFile(parentId,
                filename,
                FileConstant.FolderFlagEnum.NO,
                FileTypeContext.getFileTypeCode(filename),
                rPanFile.getFileId(),
                userId,
                rPanFile.getFileSizeDesc());
        return true;
    }

    /******************************************************私有****************************************************/

    /**
     * 拼装用户文件实体信息
     *
     * @param parentId
     * @param userId
     * @param filename
     * @param folderFlag
     * @param fileType
     * @param realFileId
     * @param fileSizeDesc
     * @return
     */
    private RPanUserFile assembleRPanUserFile(Long parentId, Long userId, String filename, FileConstant.FolderFlagEnum folderFlag, Integer fileType, Long realFileId, String fileSizeDesc) {
        RPanUserFile rPanUserFile = new RPanUserFile();
        rPanUserFile.setUserId(userId);
        rPanUserFile.setParentId(parentId);
        rPanUserFile.setFileId(IdGenerator.nextId());
        rPanUserFile.setFilename(filename);
        rPanUserFile.setFileType(fileType);
        rPanUserFile.setFolderFlag(folderFlag.getCode());
        rPanUserFile.setFileSizeDesc(fileSizeDesc);
        rPanUserFile.setRealFileId(realFileId);
        rPanUserFile.setCreateUser(userId);
        rPanUserFile.setCreateTime(new Date());
        rPanUserFile.setUpdateUser(userId);
        rPanUserFile.setUpdateTime(new Date());
        handleDuplicateFileName(rPanUserFile);
        return rPanUserFile;
    }

    /**
     * 保存用户文件信息
     *
     * @param parentId
     * @param filename
     * @param folderFlag
     * @param fileType
     * @param realFileId
     * @param userId
     * @param fileSizeDesc
     * @return
     */
    private void saveUserFile(Long parentId, String filename, FileConstant.FolderFlagEnum folderFlag, Integer fileType, Long realFileId, Long userId, String fileSizeDesc) {
        final RPanUserFile rPanUserFile = assembleRPanUserFile(parentId, userId, filename, folderFlag, fileType, realFileId, fileSizeDesc);
        final RPanUserFile save = rPanUserFilePagingRepository.save(rPanUserFile);
    }

    /**
     * 处理重复文件名
     *
     * @param rPanUserFile
     */
    private void handleDuplicateFileName(RPanUserFile rPanUserFile) {
        String newFileName = rPanUserFile.getFilename(),
                newFileNameWithoutSuffix,
                newFileNameSuffix;
        int newFileNamePointPosition = newFileName.lastIndexOf(CommonConstant.POINT_STR);
        if (newFileNamePointPosition == CommonConstant.MINUS_ONE_INT) {
            newFileNameWithoutSuffix = newFileName;
            newFileNameSuffix = CommonConstant.EMPTY_STR;
        } else {
            newFileNameWithoutSuffix = newFileName.substring(CommonConstant.ZERO_INT, newFileNamePointPosition);
            newFileNameSuffix = FileUtil.getFileSuffix(newFileName);
        }
        List<RPanUserFileVO> rPanUserFileVOList = rPanUserFileRepository.selectRPanUserFileVOListByUserIdAndFileTypeAndParentIdAndDelFlag(rPanUserFile.getUserId(), null, rPanUserFile.getParentId(), FileConstant.DelFlagEnum.NO.getCode());
        boolean noDuplicateFileNameFlag = rPanUserFileVOList.stream().noneMatch(rPanUserFileVO -> rPanUserFile.getFilename().equals(rPanUserFileVO.getFilename()));
        if (noDuplicateFileNameFlag) {
            return;
        }
        List<String> duplicateFileNameList = rPanUserFileVOList.stream()
                .map(RPanUserFileVO::getFilename)
                .filter(fileName -> fileName.startsWith(newFileNameWithoutSuffix))
                .filter(fileName -> {
                    int pointPosition = fileName.lastIndexOf(CommonConstant.POINT_STR);
                    String fileNameSuffix = CommonConstant.EMPTY_STR;
                    if (pointPosition != CommonConstant.MINUS_ONE_INT) {
                        fileNameSuffix = FileUtil.getFileSuffix(fileName);
                    }
                    return Objects.equals(newFileNameSuffix, fileNameSuffix);
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(duplicateFileNameList)) {
            return;
        }
        newFileName = new StringBuilder(newFileNameWithoutSuffix)
                .append(FileConstant.CN_LEFT_PARENTHESES_STR)
                .append((duplicateFileNameList.size()))
                .append(FileConstant.CN_RIGHT_PARENTHESES_STR)
                .append(newFileNameSuffix)
                .toString();
        rPanUserFile.setFilename(newFileName);
    }

    /**
     * 执行下载文件
     *
     * @param filePath
     * @param response
     * @param filename
     */
    private void doDownload(String filePath, HttpServletResponse response, String filename) {
        addCommonResponseHeader(response, FileConstant.APPLICATION_OCTET_STREAM_STR);
        try {
            response.setHeader(FileConstant.CONTENT_DISPOSITION_STR, FileConstant.CONTENT_DISPOSITION_VALUE_PREFIX_STR + new String(filename.getBytes(FileConstant.GB2312_STR), FileConstant.IOS_8859_1_STR));
        } catch (UnsupportedEncodingException e) {
            log.error("下载文件失败", e);
        }
        read2OutputStream(filePath, response);
    }

    /**
     * 补全要复制的文件列表
     *
     * @param toBeCopiedFileInfoList
     * @param targetParentId
     * @param userId
     */
    private void complementToBeCopiedFileInfoList(final List<RPanUserFile> toBeCopiedFileInfoList, final Long targetParentId, final Long userId) {
        final List<RPanUserFile> allChildUserFileList = Lists.newArrayList();
        toBeCopiedFileInfoList.stream().forEach(rPanUserFile -> {
            Long fileId = rPanUserFile.getFileId(),
                    newFileId = IdGenerator.nextId();
            rPanUserFile.setParentId(targetParentId);
            rPanUserFile.setUserId(userId);
            rPanUserFile.setFileId(newFileId);
            rPanUserFile.setCreateUser(userId);
            rPanUserFile.setCreateTime(new Date());
            rPanUserFile.setUpdateUser(userId);
            rPanUserFile.setUpdateTime(new Date());
            handleDuplicateFileName(rPanUserFile);
            if (checkIsFolder(rPanUserFile)) {
                assembleAllChildUserFile(allChildUserFileList, fileId, newFileId, userId);
            }
        });
        toBeCopiedFileInfoList.addAll(allChildUserFileList);
    }

    /**
     * 文件预览
     *
     * @param filePath
     * @param response
     */
    private void preview(String filePath, HttpServletResponse response, String filePreviewContentType) {
        addCommonResponseHeader(response, filePreviewContentType);
        read2OutputStream(filePath, response);
    }

    /**
     * 添加公用的响应头
     *
     * @param response
     * @param contentTypeValue
     */
    private void addCommonResponseHeader(HttpServletResponse response, String contentTypeValue) {
        response.reset();
        HttpUtil.addCorsResponseHeader(response);
        response.setHeader(FileConstant.CONTENT_TYPE_STR, contentTypeValue);
        response.setContentType(contentTypeValue);
    }

    /**
     * 文件写入响应实体
     *
     * @param filePath
     * @param response
     */
    private void read2OutputStream(String filePath, HttpServletResponse response) {
        try {
            //todo storageManager.read2OutputStream(filePath, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件写入响应实体失败", e);
        }
    }

    /**
     * 转移一个文件
     *
     * @param rPanUserFile
     * @param targetParentId
     */
    private void transferOne(RPanUserFile rPanUserFile, Long targetParentId) {
        if (rPanUserFile.getParentId().equals(targetParentId)) {
            return;
        }
        rPanUserFile.setParentId(targetParentId);
        rPanUserFile.setUpdateUser(rPanUserFile.getCreateUser());
        rPanUserFile.setUpdateTime(new Date());
        handleDuplicateFileName(rPanUserFile);
        // 修改文件信息
        rPanUserFilePagingRepository.save(rPanUserFile);
    }

    /**
     * 检查目标文件是不是子文件夹
     *
     * @param targetParentId
     * @param rPanUserFile
     * @return
     */
    private boolean checkIsChildFolder(Long targetParentId, RPanUserFile rPanUserFile) {
        if (checkIsFolder(rPanUserFile)) {
            List<RPanUserFile> allChildrenFile = Lists.newArrayList();
            findAllChildUserFile(allChildrenFile, rPanUserFile.getFileId());
            if (!CollectionUtils.isEmpty(allChildrenFile)) {
                List<Long> childFolderIdList = allChildrenFile.stream().filter(this::checkIsFolder).map(RPanUserFile::getFileId).collect(Collectors.toList());
                if (childFolderIdList.contains(targetParentId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 上传真实文件
     *
     * @param file
     * @param userId
     * @param md5
     * @param size
     * @return
     */
    private RPanFile uploadRealFile(MultipartFile file, Long userId, String md5, Long size,String storageType) {
        return iFileService.save(file, userId, md5, size,storageType);
    }

    /**
     * 拼装文件夹树
     *
     * @param folderList
     * @return
     */
    private List<FolderTreeNodeVO> assembleFolderTree(List<RPanUserFile> folderList) {
        if (CollectionUtils.isEmpty(folderList)) {
            return Lists.newArrayList();
        }
        List<FolderTreeNodeVO> folderTreeNodeList = folderList.stream().map(FolderTreeNodeVO::assembleFolderTreeNode).collect(Collectors.toList());
        Map<Long, List<FolderTreeNodeVO>> directoryTreeNodeParentGroup = folderTreeNodeList.stream().collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
        folderTreeNodeList.stream().forEach(node -> {
            List<FolderTreeNodeVO> children = directoryTreeNodeParentGroup.get(node.getId());
            if (!CollectionUtils.isEmpty(children)) {
                node.setChildren(children);
            }
        });
        return folderTreeNodeList.stream().filter(node -> Objects.equals(CommonConstant.ZERO_LONG, node.getParentId())).collect(Collectors.toList());
    }

    /**
     * 校验是不是文件夹
     *
     * @param fileId
     * @return
     */
    private boolean checkIsFolder(Long fileId) {
        RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(fileId).orElseThrow(RuntimeException::new);
        return checkIsFolder(rPanUserFile);
    }

    /**
     * 校验是不是文件夹
     *
     * @param fileId
     * @param userId
     * @return
     */
    private boolean checkIsFolder(Long fileId, Long userId) {
        return checkIsFolder(rPanUserFilePagingRepository.selectByFileIdAndUserId(fileId, userId));
    }

    /**
     * 校验是不是文件夹
     *
     * @param rPanUserFile
     * @return
     */
    private boolean checkIsFolder(RPanUserFile rPanUserFile) {
        if (Objects.isNull(rPanUserFile)) {
            throw new RPanException("文件不存在");
        }
        return Objects.equals(FileConstant.FolderFlagEnum.YES.getCode(), rPanUserFile.getFolderFlag());
    }

    /**
     * 校验是否是文件夹
     *
     * @param rPanUserFileVO
     * @return
     */
    private boolean checkIsFolder(RPanUserFileVO rPanUserFileVO) {
        if (Objects.isNull(rPanUserFileVO)) {
            throw new RPanException("文件不存在");
        }
        return Objects.equals(FileConstant.FolderFlagEnum.YES.getCode(), rPanUserFileVO.getFolderFlag());
    }

    /**
     * 查找并拼装子文件
     *
     * @param allChildUserFileList
     * @param parentUserFileId
     * @param newParentUserFileId
     * @param userId
     * @return
     */
    private void assembleAllChildUserFile(final List<RPanUserFile> allChildUserFileList, Long parentUserFileId, Long newParentUserFileId, Long userId) {
        List<RPanUserFile> childUserFileList = rPanUserFilePagingRepository.selectAvailableListByParentId(parentUserFileId);
        if (CollectionUtils.isEmpty(childUserFileList)) {
            return;
        }
        childUserFileList.stream().forEach(childUserFile -> {
            Long fileId = childUserFile.getFileId(),
                    newFileId = IdGenerator.nextId();
            childUserFile.setParentId(newParentUserFileId);
            childUserFile.setUserId(userId);
            childUserFile.setFileId(newFileId);
            childUserFile.setCreateUser(userId);
            childUserFile.setCreateTime(new Date());
            childUserFile.setUpdateUser(userId);
            childUserFile.setUpdateTime(new Date());
            allChildUserFileList.add(childUserFile);
            if (checkIsFolder(childUserFile)) {
                assembleAllChildUserFile(allChildUserFileList, fileId, newFileId, userId);
            }
        });
    }

    /**
     * 校验是不是该物理文件还在使用
     *
     * @param realFileId
     * @return
     */
    private boolean checkRealFileUsed(Long realFileId) {
        return rPanUserFilePagingRepository.selectCountByRealFileId(realFileId) > CommonConstant.ZERO_INT;
    }

    /**
     * 查询文件的所有子节点
     *
     * @param allChildUserFileList
     * @param parentId
     */
    private void findAllChildUserFile(final List<RPanUserFile> allChildUserFileList, Long parentId) {
        List<RPanUserFile> childUserFileList = rPanUserFilePagingRepository.selectAllListByParentId(parentId);
        if (CollectionUtils.isEmpty(childUserFileList)) {
            return;
        }
        allChildUserFileList.addAll(childUserFileList);
        childUserFileList.stream()
                .filter(rPanUserFile -> checkIsFolder(rPanUserFile))
                .forEach(rPanUserFile -> findAllChildUserFile(allChildUserFileList, rPanUserFile.getFileId()));
    }

    /**
     * 保存用户搜索关键字信息
     *
     * @param keyword
     * @param userId
     */
    private void saveUserSearchHistory(String keyword, Long userId) {
        iUserSearchHistoryService.save(keyword, userId);
    }

    /**
     * 拼装所有的与删除的用户文件信息列表
     *
     * @param fileIds
     * @return
     */
    private List<RPanUserFile> assembleAllToBeDeletedUserFileList(String fileIds) {
        List<RPanUserFile> rPanUserFileList = rPanUserFilePagingRepository.selectListByFileIdList(StringListUtil.string2LongList(fileIds));
        final List<RPanUserFile> allChildUserFileList = Lists.newArrayList();
        rPanUserFileList.stream()
                .filter(rPanUserFile -> checkIsFolder(rPanUserFile))
                .forEach(rPanUserFile -> findAllChildUserFile(allChildUserFileList, rPanUserFile.getFileId()));
        rPanUserFileList.addAll(allChildUserFileList);
        return rPanUserFileList;
    }

    /**
     * 拼装要删除的真实文件id列表
     *
     * @param rPanUserFileList
     * @return
     */
    private Set<Long> assembleAllUnusedRealFileIdSet(List<RPanUserFile> rPanUserFileList) {
        Set<Long> realFileIdSet = rPanUserFileList.stream()
                .filter(rPanUserFile -> !checkIsFolder(rPanUserFile))
                .filter(rPanUserFile -> !checkRealFileUsed(rPanUserFile.getRealFileId()))
                .map(RPanUserFile::getRealFileId)
                .collect(Collectors.toSet());
        return realFileIdSet;
    }

    /**
     * 保存复制文件
     *
     * @param fileIds
     * @param targetParentId
     * @param userId
     * @return
     */
    private void doCopyUserFiles(String fileIds, Long targetParentId, Long userId) {
        // 查询所有要被复制的文件信息
        List<RPanUserFile> toBeCopiedFileInfoList = rPanUserFilePagingRepository.selectListByFileIdList(StringListUtil.string2LongList(fileIds));
        complementToBeCopiedFileInfoList(toBeCopiedFileInfoList, targetParentId, userId);
        // 批量新增文件信息
        final Iterable<RPanUserFile> rPanUserFiles = rPanUserFilePagingRepository.saveAll(toBeCopiedFileInfoList);
        final Iterator<RPanUserFile> iterator = rPanUserFiles.iterator();
    }

    /**
     * 递归查找所有子文件信息
     *
     * @param allRPanUserFileVOList
     * @param rPanUserFileVO
     */
    private void findAllAvailableChildUserFile(List<RPanUserFileVO> allRPanUserFileVOList, RPanUserFileVO rPanUserFileVO) {
        if (!checkIsFolder(rPanUserFileVO)) {
            return;
        }
        List<RPanUserFileVO> rPanUserFileVOList = rPanUserFilePagingRepository.selectAvailableRPanUserFileVOListByParentId(rPanUserFileVO.getFileId());
        if (CollectionUtils.isEmpty(rPanUserFileVOList)) {
            return;
        }
        allRPanUserFileVOList.addAll(rPanUserFileVOList);
        rPanUserFileVOList.stream().forEach(rPanUserFileVOItem -> findAllAvailableChildUserFile(allRPanUserFileVOList, rPanUserFileVOItem));
    }

    /**
     * 根据用户id和文件id查询对应的文件信息
     *
     * @param fileId
     * @param userId
     * @return
     */
    private RPanUserFile getRPanUserFileByFileIdAndUserId(Long fileId, Long userId) {
        RPanUserFile originalUserFileInfo = rPanUserFilePagingRepository.findById(fileId).orElseThrow(RuntimeException::new);
        if (Objects.isNull(originalUserFileInfo)) {
            throw new RPanException("该用户没有此文件");
        }
        if (!Objects.equals(originalUserFileInfo.getUserId(), userId)) {
            throw new RPanException("该用户没有此文件");
        }
        if (Objects.equals(FileConstant.DelFlagEnum.YES.getCode(), originalUserFileInfo.getDelFlag())) {
            throw new RPanException("该用户没有此文件");
        }
        return originalUserFileInfo;
    }

    /**
     * 校验目标文件夹是否合法
     * 1、 不是选中的文件夹
     * 2、 不是选中文件夹的子文件夹
     *
     * @param targetParentId
     * @param fileIds
     * @return
     */
    private boolean checkTargetParentIdAvailable(Long targetParentId, String fileIds) {
        List<Long> fileIdList = StringListUtil.string2LongList(fileIds);
        if (fileIdList.contains(targetParentId)) {
            return false;
        }
        for (Long fileId : fileIdList) {
            final RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(fileId).orElseThrow(RuntimeException::new);
            if (checkIsChildFolder(targetParentId, rPanUserFile)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 递归查询所有有效的子节点id
     *
     * @param allAvailableFileId
     * @param fileId
     */
    private void findAllAvailableFileIdByFileId(List<Long> allAvailableFileId, Long fileId) {
        if (!checkIsFolder(fileId)) {
            return;
        }
        List<Long> childrenFileIds = rPanUserFilePagingRepository.selectAvailableFileIdListByParentId(fileId);
        if (CollectionUtils.isEmpty(childrenFileIds)) {
            return;
        }
        allAvailableFileId.addAll(childrenFileIds);
        childrenFileIds.stream().forEach(childrenFileId -> findAllAvailableFileIdByFileId(allAvailableFileId, childrenFileId));
    }

    /**
     * 校验当前文件和所有父文件夹是否均正常
     *
     * @param fileId
     * @return
     */
    private boolean checkUpFileAvailable(Long fileId) {
        RPanUserFile rPanUserFile = rPanUserFilePagingRepository.findById(fileId).orElseThrow(RuntimeException::new);
        if (rPanUserFile.getDelFlag().equals(FileConstant.DelFlagEnum.YES.getCode())) {
            return false;
        }
        if (rPanUserFile.getParentId().equals(CommonConstant.ZERO_LONG)) {
            return true;
        }
        return checkUpFileAvailable(rPanUserFile.getParentId());
    }

    /**
     * 上传分片并自动合并并转移
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
    private RPanFile uploadRealFileWithChunk(MultipartFile file, Long userId, String md5, Integer chunks, Integer chunk, Long size, String name,String storageType) {
        return iFileService.saveWithChunk(file, userId, md5, chunks, chunk, size, name,storageType);
    }

}