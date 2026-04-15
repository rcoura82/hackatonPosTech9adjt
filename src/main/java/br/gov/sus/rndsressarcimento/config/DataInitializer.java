package br.gov.sus.rndsressarcimento.config;

import br.gov.sus.rndsressarcimento.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(AuthService authService) {
        return args -> authService.seedDefaultUsers();
    }
}
