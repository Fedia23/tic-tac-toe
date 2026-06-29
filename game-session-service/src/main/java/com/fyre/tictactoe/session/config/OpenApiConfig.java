package com.fyre.tictactoe.session.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gameSessionServiceAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8082");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Game Session Service Team");
        contact.setEmail("support@fyre.tictactoe.com");

        Info info = new Info()
            .title("Game Session Service API")
            .version("1.0.0")
            .description("REST API for managing tic-tac-toe game sessions")
            .contact(contact);

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer));
    }
}