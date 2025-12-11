package Commerce360.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**", "/api/users/register").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/metrics/**").permitAll()
                        
                        // Swagger UI and OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()

                        // Inventory - ADMIN and STORE_MANAGER
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "STORE_MANAGER")

                        // Products - GET for all authenticated, modifications for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Suppliers - GET for authenticated, modifications for ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasRole("ADMIN")

                        // Supplier Products - Browse public, manage by SUPPLIER/ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/supplier-products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/supplier-products/**").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/supplier-products/**").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/supplier-products/**").hasAnyRole("SUPPLIER", "ADMIN")

                        // Purchase Orders - B2B flow (STORE_MANAGER and SUPPLIER)
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("STORE_MANAGER", "SUPPLIER", "ADMIN")

                        // Stores - Accessible by ADMIN and STORE_MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")

                        // Users - ADMIN manages, users can view/update own profile
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // Customer endpoints (to be implemented)
                        .requestMatchers("/api/customers/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "STORE_MANAGER", "ADMIN")

                        // Catalog browsing - public
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()

                        // Audit logs - ADMIN only
                        .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
