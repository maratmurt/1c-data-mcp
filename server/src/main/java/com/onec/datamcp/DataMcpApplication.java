package com.onec.datamcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.onec.datamcp.configuration.DataMcpProperties;

@SpringBootApplication
@EnableConfigurationProperties(DataMcpProperties.class)
public class DataMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataMcpApplication.class, args);
    }
}
