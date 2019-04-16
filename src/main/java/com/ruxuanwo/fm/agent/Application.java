package com.ruxuanwo.fm.agent;

import com.ruxuanwo.fm.agent.filter.ServiceFilter;
import com.ruxuanwo.fm.client.client.FileManagerClient;
import com.ruxuanwo.fm.client.client.impl.DefaultFileManagerClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public FileManagerClient getFileManagerClient() {
        return new DefaultFileManagerClient();
    }
    /**
     * 这个Filter 解决页面跨域访问问题
     */
    @Bean
    public FilterRegistrationBean omsFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ServiceFilter());
        registration.addUrlPatterns("/*");
        registration.setName("MainFilter");
        registration.setAsyncSupported(true);
        registration.setOrder(1);
        return registration;
    }

}
