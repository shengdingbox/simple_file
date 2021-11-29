package com.zhouzifei.simplefile.modules.share.controller;

import com.zhouzifei.simplefile.common.annotation.NeedLogin;
import com.zhouzifei.simplefile.common.annotation.NeedShareCode;
import com.zhouzifei.simplefile.common.response.R;
import com.zhouzifei.simplefile.util.ShareIdUtil;
import com.zhouzifei.simplefile.util.UserIdUtil;
import com.zhouzifei.simplefile.modules.file.vo.RPanUserFileVO;
import com.zhouzifei.simplefile.modules.share.po.CancelSharePO;
import com.zhouzifei.simplefile.modules.share.po.CheckShareCodePO;
import com.zhouzifei.simplefile.modules.share.po.CreateSharePO;
import com.zhouzifei.simplefile.modules.share.po.ShareSavePO;
import com.zhouzifei.simplefile.modules.share.service.IShareService;
import com.zhouzifei.simplefile.modules.share.vo.RPanUserShareDetailVO;
import com.zhouzifei.simplefile.modules.share.vo.RPanUserShareSimpleDetailVO;
import com.zhouzifei.simplefile.modules.share.vo.RPanUserShareUrlVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 项目分享相关rest接口返回
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
@RestController
@Validated
@Api(tags = "分享")
public class ShareController {

    @Autowired
    @Qualifier(value = "shareService")
    private IShareService iShareService;

    /**
     * 创建分享链接
     *
     * @param createSharePO
     * @return
     */
    @ApiOperation(
            value = "创建分享链接",
            notes = "该接口提供了创建分享链接的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("share")
    @NeedLogin
    public R<RPanUserShareUrlVO> create(@Validated @RequestBody CreateSharePO createSharePO) {
        return R.data(iShareService.create(createSharePO.getShareName(), createSharePO.getShareType(), createSharePO.getShareDayType(), createSharePO.getShareFileIds(), UserIdUtil.get()));
    }

    /**
     * 查询分享列表
     *
     * @return
     */
    @ApiOperation(
            value = "查询分享列表",
            notes = "该接口提供了查询分享列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("shares")
    @NeedLogin
    public R<List<RPanUserShareUrlVO>> list() {
        return R.data(iShareService.list(UserIdUtil.get()));
    }

    /**
     * 取消分享（支持批量）
     *
     * @param cancelSharePO
     * @return
     */
    @ApiOperation(
            value = "取消分享（支持批量）",
            notes = "该接口提供了取消分享（支持批量）的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("share")
    @NeedLogin
    public R cancel(@Validated @RequestBody CancelSharePO cancelSharePO) {
        iShareService.cancel(cancelSharePO.getShareIds(), UserIdUtil.get());
        return R.success();
    }

    /**
     * 获取分享详情
     *
     * @return
     */
    @ApiOperation(
            value = "获取分享详情",
            notes = "该接口提供了获取分享详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("share")
    @NeedShareCode
    public R<RPanUserShareDetailVO> detail() {
        return R.data(iShareService.detail(ShareIdUtil.get()));
    }

    /**
     * 获取分享详情-简单
     *
     * @return
     */
    @ApiOperation(
            value = "获取简单分享详情",
            notes = "该接口提供了获取简单分享详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("share/simple")
    public R<RPanUserShareSimpleDetailVO> simpleDetail(@NotNull(message = "分享ID不能为空") @RequestParam(value = "shareId", required = false) Long shareId) {
        return R.data(iShareService.simpleDetail(shareId));
    }

    /**
     * 校验分享码
     *
     * @param checkShareCodePO
     * @return
     */
    @ApiOperation(
            value = "校验分享码",
            notes = "该接口提供了校验分享码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("share/code/check")
    public R<String> checkShareCode(@Validated @RequestBody CheckShareCodePO checkShareCodePO) {
        return R.data(iShareService.checkShareCode(checkShareCodePO.getShareId(), checkShareCodePO.getShareCode()));
    }

    /**
     * 获取下一级的文件列表
     *
     * @param parentId
     * @return
     */
    @ApiOperation(
            value = "获取下一级的文件列表",
            notes = "该接口提供了获取下一级的文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("share/file/list")
    @NeedShareCode
    public R<List<RPanUserFileVO>> fileList(@NotNull(message = "父id不能为空") @RequestParam(value = "parentId", required = false) Long parentId) {
        return R.data(iShareService.fileList(ShareIdUtil.get(), parentId));
    }

    /**
     * 保存至我的网盘
     *
     * @param shareSavePO
     * @return
     */
    @ApiOperation(
            value = "保存至我的网盘",
            notes = "该接口提供了保存至我的网盘的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("share/save")
    @NeedShareCode
    @NeedLogin
    public R save(@Validated @RequestBody ShareSavePO shareSavePO) {
        iShareService.save(ShareIdUtil.get(), shareSavePO.getFileIds(), shareSavePO.getTargetParentId(), UserIdUtil.get());
        return R.success();
    }

    /**
     * 分享文件下载
     *
     * @param fileId
     * @param response
     * @return
     */
    @ApiOperation(
            value = "分享文件下载",
            notes = "该接口提供了分享文件下载的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("share/file/download")
    @NeedShareCode
    @NeedLogin
    public void download(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId,
                         HttpServletResponse response) {
        iShareService.download(ShareIdUtil.get(), fileId, response);
    }

}
