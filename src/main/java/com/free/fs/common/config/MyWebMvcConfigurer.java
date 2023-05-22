package com.free.fs.common.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * web配置
 *
 * @author dinghao
 * @date 2021/3/10
 */
@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * 支持跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").maxAge(3600).allowedHeaders("Content-Type, x-requested-with, X-Custom-Header, Authorization");
    }

    /**
     * 支持put请求
     */
    @Bean
    public HttpPutFormContentFilter httpPutFormContentFilter() {
        return new HttpPutFormContentFilter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OptionsInterceptor()).addPathPatterns("/**");
    }

    @SneakyThrows
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        File fileDir = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "templates");
        final String fileDirPath = fileDir.getPath();
        if (!fileDir.exists()) {
            System.out.println(fileDirPath);
            System.out.println("文件目录问题");
        }
        System.out.println(fileDirPath);
        List<String> list = new ArrayList<>();
        getFileName(fileDir, list);
        final List<String> collect = list.stream().map(t -> t.replace(fileDirPath, "")).collect(Collectors.toList());
        for (String s : collect) {
            registry.addViewController(s).setViewName(s.replace(".html",""));
        }
    }

    public void getFileName(File fileDir, List<String> fileNames) {
        File[] files = fileDir.listFiles();
        if (files == null) {
            System.out.println("文件为空");
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getFileName(file, fileNames);
            }
            if (file.isFile()) {
                fileNames.add(file.getPath());
            }
            //System.out.println(file.getPath());
        }
    }
}
