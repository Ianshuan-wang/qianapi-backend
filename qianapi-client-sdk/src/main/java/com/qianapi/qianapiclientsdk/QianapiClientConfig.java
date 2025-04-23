package com.qianapi.qianapiclientsdk;

import com.qianapi.qianapiclientsdk.client.QianapiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// 通过 @Configuration 注解,将该类标记为一个配置类,告诉 Spring 这是一个用于配置的类
@Configuration
// 能够读取application.yml的配置,读取到配置之后,把这个读到的配置设置到我们这里的属性中,
// 这里给所有的配置加上前缀为"qianapi.client"
@ConfigurationProperties("qianapi.client")
@Data
@ComponentScan
public class QianapiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public QianapiClient qianapiClient() {
        return new QianapiClient(accessKey, secretKey);
    }
}
