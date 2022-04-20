package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j //日志输出
@SpringBootApplication
/*
    在SpringBootApplication上使用@ServletComponentScan注解后，Servlet、Filter、Listener
    可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册，无需其他代码。
*/
@ServletComponentScan
@EnableTransactionManagement //开启事务注解
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功...");
    }
}
