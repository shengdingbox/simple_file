package com.free.fs.controller;

import com.zhouzifei.tool.consts.StorageTypeConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class IndexController extends BaseController {

    /**
     * 主页
     */
    @GetMapping({"", "/index"})
    public String index(Model model) {
        if (getLoginUser() == null) {
            return "redirect:login";
        }
        // 登录用户信息
        model.addAttribute("fileType", getFileType());
        model.addAttribute("loginUser", getLoginUser());
        return "index";
    }

    private Map<String, String> getFileType() {
        final Map<String, String> map = StorageTypeConst.getMap();
        return map;
    }

    @GetMapping("/fileChoose")
    public String fileChoose() {

        return "fileChoose";
    }

    /**
     * 错误页
     */
    @RequestMapping("/error/{code}")
    public String error(@PathVariable("code") String code) {
        if ("403".equals(code)) {
            return "error/403";
        } else if ("500".equals(code)) {
            return "error/500";
        }
        return "error/404";
    }
}
