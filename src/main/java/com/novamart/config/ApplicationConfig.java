package com.novamart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}
