package br.gov.sus.rndsressarcimento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("RNDS Ressarcimento SUS API")
                .description("Plataforma de ressarcimento SUS-operadoras com trilha imutavel em blockchain")
                .version("v1")
                .contact(new Contact().name("Equipe Hackaton PosTech"))
                .license(new License().name("Uso interno MVP")));
    }
}
