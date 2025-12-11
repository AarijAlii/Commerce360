package Commerce360.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Commerce360 Marketplace API")
                        .version("1.0.0")
                        .description(
                                "Multi-sided marketplace platform supporting B2B (Supplier ↔ Store Manager) and B2C (Customer ↔ Store) commerce with complete admin oversight.")
                        .contact(new Contact()
                                .name("Commerce360 Team")
                                .email("support@commerce360.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://commerce360.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:10000")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.commerce360.com")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login")));
    }
}
