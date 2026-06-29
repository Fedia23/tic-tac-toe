package com.fyre.tictactoe.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Service Discovery Server
 *
 * This server acts as a registry for all microservices in the Tic Tac Toe application.
 * Services register themselves on startup and can discover each other through this server.
 *
 * Default port: 8761
 * Dashboard URL: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}