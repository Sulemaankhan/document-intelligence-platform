package com.dip;

import com.dip.config.DipAiProperties;
import com.dip.config.DipChunkProperties;
import com.dip.config.DipOcrProperties;
import com.dip.config.DipRagProperties;
import com.dip.config.DipStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DipAiProperties.class, DipStorageProperties.class, DipChunkProperties.class, DipRagProperties.class, DipOcrProperties.class})
public class DipApplication {

    public static void main(String[] args) {
        SpringApplication.run(DipApplication.class, args);
    }
}
