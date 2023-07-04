package com.free.fs.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * web配置
 *
 * @author dinghao
 * @date 2021/3/10
 */
@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    @Value("${datasource.db.path}")
    String dbPath;

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
        final File file = new File(dbPath + "/simple-file.db");
        if (!file.exists()) {
           //数据库文件不存在
            File fileDir = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "simple-file.db");
            try (InputStream is = Files.newInputStream(fileDir.toPath()); OutputStream os = Files.newOutputStream(file.toPath())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
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
