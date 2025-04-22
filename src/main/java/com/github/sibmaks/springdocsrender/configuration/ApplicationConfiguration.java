package com.github.sibmaks.springdocsrender.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public Base64.Encoder base64Encoder() {
        return Base64.getEncoder();
    }

    @Bean
    public Base64.Decoder base64Decoder() {
        return Base64.getDecoder();
    }

}
