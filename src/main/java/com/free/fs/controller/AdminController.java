package com.free.fs.controller;

import com.alibaba.fastjson.JSONObject;
import com.free.fs.controller.request.FileTypeRequest;
import com.free.fs.dto.MenuDTO;
import com.free.fs.model.Menu;
import com.free.fs.service.FileService;
import com.free.fs.utils.R;
import com.zhouzifei.tool.common.ServiceException;
import com.zhouzifei.tool.config.SimpleFsProperties;
import com.zhouzifei.tool.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LoginController
 */
@Controller
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private final FileService fileService;

    @Autowired
    private SimpleFsProperties simpleFsProperties;

    @GetMapping("/")
    public String admin() {
        return "index";
    }

    @GetMapping("/list.html")
    public String list(String source, Model model) {
        model.addAttribute("source", source);
        return "list";
    }

    @GetMapping("/welcome.html")
    public String welcome(String source, Model model) {
        model.addAttribute("source", source);
        return "welcome";
    }

    /**
     * 注册页
     */
    @GetMapping("/add.html")
    public String add(Model model, FileTypeRequest fileTypeRequest) {
        final String source = fileTypeRequest.getSource();
        final Class<? extends SimpleFsProperties> aClass = simpleFsProperties.getClass();
        Map<String, String> names = new HashMap<>();
        try {
            final Field declaredField = aClass.getDeclaredField(source);
            declaredField.setAccessible(true);
            final Class<?> type = declaredField.getType();
            final Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final String name = field.getName();
                names.put(name, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("fileType", names);
        model.addAttribute("storageType", source);
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
    public R menu() {
        List<Menu> menus;
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("json/menu.json")) {
            final String parseInputStream = FileUtil.parseInputStream(inputStream);
            menus = JSONObject.parseArray(parseInputStream, Menu.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final List<MenuDTO> collect = menus.stream().map(MenuDTO::of).collect(Collectors.toList());
        return R.of(collect, 1, "ok");
    }
}
