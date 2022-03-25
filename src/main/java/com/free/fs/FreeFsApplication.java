package com.free.fs;

import com.zhouzifei.tool.config.FileAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * free-fs
 *
 * @author dinghao
 * @date 2021/3/16
 */
@SpringBootApplication
@EnableConfigurationProperties(FileAutoConfiguration.class)
public class FreeFsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreeFsApplication.class, args);
    }

}
