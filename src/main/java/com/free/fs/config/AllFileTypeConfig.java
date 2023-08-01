package com.free.fs.config;

import com.alibaba.fastjson.JSONObject;
import com.free.fs.model.Menu;
import com.zhouzifei.tool.config.SimpleFsProperties;
import com.zhouzifei.tool.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AllFileTypeConfig {
    @Autowired
    SimpleFsProperties simpleFsProperties;

    @PostConstruct
    public void init() {
        log.info("项目启动中，加载用户数据");
        final ArrayList<Menu> fileTypes = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("json/menu.json")) {
            final String parseInputStream = FileUtil.parseInputStream(inputStream);
            final List<Menu> menus = JSONObject.parseArray(parseInputStream, Menu.class);
            fileTypes.addAll(menus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Map<String, Menu> collect = fileTypes.stream().collect(Collectors.toMap(Menu::getSource, menu -> menu, (a, b) -> b));
        final Class<? extends SimpleFsProperties> aClass = simpleFsProperties.getClass();
        final Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            try {
                final String name = declaredField.getName();
                final Class<?> type = declaredField.getType();
                Menu menu = collect.get(name);
                if (null == menu) {
                    menu = new Menu();
                    menu.setSource(name);
                }
                String config = menu.getConfig();
                if (StringUtils.isEmpty(config)) {
                    config = "{}";
                }
                final Object o = JSONObject.parseObject(config, type);
                declaredField.set(simpleFsProperties, o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("项目启动中，加载用户数据完成-->" + simpleFsProperties);
    }
}
