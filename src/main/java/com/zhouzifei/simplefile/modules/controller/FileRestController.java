package com.zhouzifei.simplefile.modules.controller;

import com.zhouzifei.simplefile.common.annotation.NeedLogin;
import com.zhouzifei.simplefile.common.response.R;
import com.zhouzifei.simplefile.modules.bo.FileTypeBO;
import com.zhouzifei.simplefile.modules.po.*;
import com.zhouzifei.simplefile.modules.service.IUserFileService;
import com.zhouzifei.simplefile.modules.vo.BreadcrumbVO;
import com.zhouzifei.simplefile.modules.vo.FolderTreeNodeVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileSearchVO;
import com.zhouzifei.simplefile.modules.vo.RPanUserFileVO;
import com.zhouzifei.simplefile.util.StorageUtil;
import com.zhouzifei.simplefile.util.UserIdUtil;
import com.zhouzifei.tool.consts.StorageTypeConst;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 项目文件相关rest接口返回
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@RestController
@Validated
@Api(tags = "文件接口")
public class FileRestController {

    @Autowired
    @Qualifier(value = "userFileService")
    private IUserFileService iUserFileService;

    /**
     * 获取文件列表
     *
     * @param parentId
     * @param fileTypes
     * @return
     */
    @ApiOperation(
            value = "获取文件列表",
            notes = "该接口提供了获取文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("files")
    @NeedLogin
    public R<List<RPanUserFileVO>> list(@NotNull(message = "父id不能为空") @RequestParam(value = "parentId", required = false) Long parentId,
                                        @RequestParam(name = "fileTypes", required = false, defaultValue = "-1") String fileTypes) {
        return R.data(iUserFileService.list(parentId, fileTypes, UserIdUtil.getUserId()));
    }
    /**
     * 获取文件列表
     *
     * @return
     */
    @ApiOperation(
            value = "获取类型列表",
            notes = "该接口提供了获取类型列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("type")
    @NeedLogin
    public R<List<FileTypeBO>> type() {
        Map<String, String> map = StorageTypeConst.getMap();
        final Set<String> stringSet = map.keySet();
        List<FileTypeBO> list = new ArrayList<>();
        for (String string : stringSet) {
            final FileTypeBO fileTypeBO = new FileTypeBO();
            fileTypeBO.setValue(string);
            fileTypeBO.setLable(map.get(string));
            list.add(fileTypeBO);
        }
        return R.data(list);
    }
    /**
     * 获取文件列表
     *
     * @return
     */
    @ApiOperation(
            value = "设置文件类型",
            notes = "该接口提供了设置文件类型的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("setType")
    @NeedLogin
    public R<List<FileTypeBO>> setType(String storageType) {
        StorageUtil.set(storageType);
        return R.success();
    }

    /**
     * 新建文件夹
     *
     * @param createFolderPO
     * @return
     */
    @ApiOperation(
            value = "新建文件夹",
            notes = "该接口提供了新建文件夹的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/folder")
    @NeedLogin
    public R createFolder(@Validated @RequestBody CreateFolderPO createFolderPO) {
        iUserFileService.createFolder(createFolderPO.getParentId(), createFolderPO.getFolderName(), UserIdUtil.getUserId());
        return R.success();
    }

    /**
     * 文件重命名
     *
     * @param updateFileNamePO
     * @return
     */
    @ApiOperation(
            value = "文件重命名",
            notes = "该接口提供了文件重命名的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("file")
    @NeedLogin
    public R updateFilename(@Validated @RequestBody UpdateFileNamePO updateFileNamePO) {
        iUserFileService.updateFilename(updateFileNamePO.getFileId(), updateFileNamePO.getNewFilename(), UserIdUtil.getUserId());
        return R.success();
    }

    /**
     * 删除文件(批量)
     *
     * @param deletePO
     * @return
     */
    @ApiOperation(
            value = "删除文件(批量)",
            notes = "该接口提供了删除文件(批量)的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("file")
    @NeedLogin
    public R delete(@Validated @RequestBody DeletePO deletePO) {
        iUserFileService.delete(deletePO.getFileIds(), UserIdUtil.getUserId());
        return R.success();
    }

    /**
     * 上传文件
     *
     * @param fileUploadPO
     * @return
     */
    @ApiOperation(
            value = "上传文件",
            notes = "该接口提供了上传文件的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/upload")
    @NeedLogin
    public R upload(@Validated FileUploadPO fileUploadPO) {
        final String  storageType = StorageUtil.get();
        if (fileUploadPO.isChunked()) {
            iUserFileService.uploadWithChunk(fileUploadPO.getFile(), fileUploadPO.getParentId(), UserIdUtil.getUserId(), fileUploadPO.getMd5(), fileUploadPO.getChunks(), fileUploadPO.getChunk(), fileUploadPO.getSize(), fileUploadPO.getName(),storageType);
        } else {
            iUserFileService.upload(fileUploadPO.getFile(), fileUploadPO.getParentId(), UserIdUtil.getUserId(), fileUploadPO.getMd5(), fileUploadPO.getSize(),storageType);
        }
        return R.success();
    }

    /**
     * 秒传文件
     *
     * @param fileSecUploadPO
     * @return
     */
    @ApiOperation(
            value = "秒传文件",
            notes = "该接口提供了秒传文件的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/sec-upload")
    @NeedLogin
    public R secUpload(@Validated @RequestBody FileSecUploadPO fileSecUploadPO) {
        if (iUserFileService.secUpload(fileSecUploadPO.getParentId(), fileSecUploadPO.getName(), fileSecUploadPO.getMd5(), UserIdUtil.getUserId())) {
            return R.success();
        }
        return R.fail("MD5不存在");
    }

    /**
     * 下载文件(只支持单个下载)
     *
     * @param fileId
     * @param response
     */
    @ApiOperation(
            value = "下载文件(只支持单个下载)",
            notes = "该接口提供了下载文件(只支持单个下载)的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("file/download")
    @NeedLogin
    public void download(@NotNull(message = "请选择要下载的文件") @RequestParam(value = "fileId", required = false) Long fileId,
                         HttpServletResponse response) {
        iUserFileService.download(fileId, response, UserIdUtil.getUserId());
    }

    /**
     * 获取文件夹树
     *
     * @return
     */
    @ApiOperation(
            value = "获取文件夹树",
            notes = "该接口提供了获取文件夹树的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("file/folder/tree")
    @NeedLogin
    public R<List<FolderTreeNodeVO>> getFolderTree() {
        return R.data(iUserFileService.getFolderTree(UserIdUtil.getUserId()));
    }

    /**
     * 转移文件(批量)
     *
     * @param transferPO
     * @return
     */
    @ApiOperation(
            value = "转移文件(批量)",
            notes = "该接口提供了转移文件(批量)的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/transfer")
    @NeedLogin
    public R transfer(@Validated @RequestBody TransferPO transferPO) {
        iUserFileService.transfer(transferPO.getFileIds(), transferPO.getTargetParentId(), UserIdUtil.getUserId());
        return R.success();
    }

    /**
     * 复制文件(批量)
     *
     * @param copyPO
     * @return
     */
    @ApiOperation(
            value = "复制文件(批量)",
            notes = "该接口提供了复制文件(批量)的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/copy")
    @NeedLogin
    public R copy(@Validated @RequestBody CopyPO copyPO) {
        iUserFileService.copy(copyPO.getFileIds(), copyPO.getTargetParentId(), UserIdUtil.getUserId());
        return R.success();
    }

    /**
     * 通过文件名搜索文件列表
     *
     * @param keyword
     * @param fileTypes
     * @return
     */
    @ApiOperation(
            value = "通过文件名搜索文件列表",
            notes = "该接口提供了通过文件名搜索文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("file/search")
    @NeedLogin
    public R<List<RPanUserFileSearchVO>> search(@NotBlank(message = "关键字不能为空") @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(name = "fileTypes", required = false, defaultValue = "-1") String fileTypes) {
        return R.data(iUserFileService.search(keyword, fileTypes, UserIdUtil.getUserId()));
    }

    /**
     * 查询文件详情
     *
     * @param fileId
     * @return
     */
    @ApiOperation(
            value = "查询文件详情",
            notes = "该接口提供了查询文件详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("file")
    @NeedLogin
    public R<RPanUserFileVO> detail(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId) {
        return R.data(iUserFileService.detail(fileId, UserIdUtil.getUserId()));
    }

    /**
     * 获取面包屑列表
     *
     * @return
     */
    @ApiOperation(
            value = "获取面包屑列表",
            notes = "该接口提供了获取面包屑列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("file/breadcrumbs")
    @NeedLogin
    public R<List<BreadcrumbVO>> getBreadcrumbs(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId) {
        return R.data(iUserFileService.getBreadcrumbs(fileId, UserIdUtil.getUserId()));
    }

    /**
     * 预览单个文件
     *
     * @param fileId
     * @return
     */
    @ApiOperation(
            value = "预览单个文件",
            notes = "该接口提供了预览单个文件的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("preview")
    @NeedLogin
    public void preview(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId,
                        HttpServletResponse response) {
        iUserFileService.preview(fileId, response, UserIdUtil.getUserId());
    }

}
