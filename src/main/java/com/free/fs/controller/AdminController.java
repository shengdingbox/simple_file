package com.free.fs.controller;

import com.alibaba.fastjson.JSONObject;
import com.free.fs.controller.request.FileTypeRequest;
import com.free.fs.dto.MenuDTO;
import com.free.fs.model.FileType;
import com.free.fs.model.Menu;
import com.free.fs.service.FileService;
import com.free.fs.service.UserService;
import com.free.fs.utils.R;
import com.zhouzifei.tool.common.ServiceException;
import com.zhouzifei.tool.config.SimpleFsProperties;
import com.zhouzifei.tool.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LoginController
 */
@Controller
@RequiredArgsConstructor
public class AdminController{

    @Autowired
    private final FileService fileService;

    @Autowired
    private SimpleFsProperties simpleFsProperties;

    @Autowired
    private UserService userService;
    @GetMapping("/")
    public String admin() {
        return "index";
    }
    @GetMapping("/list.html")
    public String list(String source,Model model) {
        model.addAttribute("source", source);
        return "list";
    }
    @GetMapping("/welcome.html")
    public String welcome(String source,Model model) {
        model.addAttribute("source", source);
        return "welcome";
    }
    /**
     * 注册页
     */
    @GetMapping("/add.html")
    public String add(Model model, FileTypeRequest fileTypeRequest) {
        final String source = fileTypeRequest.getSource();
        final Menu menu = fileService.getTypeInfo(source);
        if (null == menu) {
            return "add";
        }
        final String config = menu.getConfig();
        final Class<? extends SimpleFsProperties> aClass = simpleFsProperties.getClass();
        Map<String, String> names = new HashMap<>();
        try {
            final Field declaredField = aClass.getDeclaredField(source);
            declaredField.setAccessible(true);
            final Class<?> type = declaredField.getType();
            JSONObject jsonObject = JSONObject.parseObject(config);
            if (null == jsonObject) {
                jsonObject = new JSONObject();
            }
            final Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final String name = field.getName();
                final String value = jsonObject.getString(name);
                names.put(name, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("fileType", names);
        model.addAttribute("storageType", menu.getSource());
        return "add";
    }

    /**
     * 注册页
     */
    @PostMapping("/add")
    @ResponseBody
    public R addSource(@RequestBody String str) {
        final JSONObject jsonObject = JSONObject.parseObject(str);
        if (!jsonObject.containsKey("source")) {
            throw new ServiceException("请选择存储类型");
        }
        final String source = jsonObject.getString("source");
        final Menu menu = fileService.getTypeInfo(source);
        menu.setConfig(str);
        fileService.saveType(menu);
        try {
            final Class<? extends SimpleFsProperties> aClass = simpleFsProperties.getClass();
            final Field declaredField = aClass.getDeclaredField(source);
            declaredField.setAccessible(true);
            final Class<?> type = declaredField.getType();
            String config = menu.getConfig();
            if (StringUtils.isEmpty(config)) {
                config = "{}";
            }
            final Object o = JSONObject.parseObject(config, type);
            declaredField.set(simpleFsProperties, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.succeed("注册成功");
    }
    /**
     * 注册
     */
    @PostMapping("/admin/user/menu")
    @ResponseBody
    public R menu(String username) {
        List<MenuDTO> jsonObject = userService.getMenu();
        return R.of(jsonObject, 1, "ok");
    }
}
