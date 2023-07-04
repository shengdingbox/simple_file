package com.free.fs.controller;

import com.alibaba.fastjson.JSONObject;
import com.free.fs.controller.request.FileTypeRequest;
import com.free.fs.dto.FileTypeDTO;
import com.free.fs.model.FileType;
import com.free.fs.service.FileService;
import com.free.fs.utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * LoginController
 */
@Controller
@RequiredArgsConstructor
public class AdminController extends BaseController {

    @Autowired
    private final FileService fileService;

    @GetMapping("/admin.html")
    public String admin(){
        return "admin";
    }
    /**
     * 注册页
     */
    @GetMapping("/add.html")
    public String add(Model model,FileTypeRequest fileTypeRequest) {
        final String source = fileTypeRequest.getSource();
        final FileType fileType = fileService.getTypeInfo(source);
        final FileTypeDTO fileTypeDTO = JSONObject.parseObject(fileType.getConfig(), FileTypeDTO.class);
        model.addAttribute("fileType", fileTypeDTO);
        return "add";
    }
    /**
     * 注册页
     */
    @PostMapping("/add")
    @ResponseBody
    public R addSource(FileTypeRequest fileTypeRequest) {
        final String source = fileTypeRequest.getSource();
        final FileType fileType = fileService.getTypeInfo(source);
        final FileTypeDTO fileTypeDTO = JSONObject.parseObject(fileType.getConfig(), FileTypeDTO.class);
        return R.succeed("注册成功");
    }
}
