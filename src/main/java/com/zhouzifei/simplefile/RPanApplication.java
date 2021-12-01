package com.zhouzifei.simplefile;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.zhouzifei")
@EnableTransactionManagement
@MapperScan("com.zhouzifei.simplefile.modules.**.dao")
@ServletComponentScan(basePackages = "com.zhouzifei")
public class RPanApplication {

    public static void main(String[] args) {
        SpringApplication.run(RPanApplication.class, args);
    }

}
