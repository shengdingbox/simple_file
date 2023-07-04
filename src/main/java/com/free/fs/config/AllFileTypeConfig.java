package com.free.fs.config;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.free.fs.mapper.TypeMapper;
import com.free.fs.model.FileType;
import com.zhouzifei.tool.config.SimpleFsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AllFileTypeConfig  {

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    SimpleFsProperties simpleFsProperties;

    @PostConstruct
    public void init() {
        log.info("项目启动中，加载用户数据");
        final List<FileType> fileTypes = typeMapper.selectList(new LambdaQueryWrapper<FileType>());
        //simpleFsProperties.
        final Map<String, FileType> collect = fileTypes.stream().collect(Collectors.toMap(FileType::getStorageType, fileType -> fileType));
        final Class<? extends SimpleFsProperties> aClass = simpleFsProperties.getClass();
        final Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            try {
                final String name = declaredField.getName();
                final Class<?> type = declaredField.getType();
                final FileType fileType = collect.get(name);
                if(null == fileType){
                    continue;
                }
                 String config = fileType.getConfig();
                if(StringUtils.isEmpty(config)){
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
