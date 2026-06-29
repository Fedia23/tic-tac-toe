package com.fyre.tictactoe.session.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.fyre.tictactoe.session.client")
public class FeignConfig {
}