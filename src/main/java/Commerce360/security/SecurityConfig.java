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
                        .requestMatchers("/api/auth/**", "/api/users/register").permitAll()
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        // Products - GET requests allowed for both roles, others for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        // Suppliers - GET requests allowed for both roles, others for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasRole("ADMIN")
                        // Stores - GET requests allowed for both roles, others for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/stores/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        // Users - GET requests allowed for both roles, others for ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        // Allow store managers to update their own information
                        .requestMatchers("/api/users/me/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/metrics/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
