package com.free.fs.controller;

import com.free.fs.model.FileType;
import com.free.fs.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String add(String source, Model model) {
        final FileType fileType = fileService.getTypeInfo(source);
        model.addAttribute("fileType", fileType);
        return "add";
    }
}
