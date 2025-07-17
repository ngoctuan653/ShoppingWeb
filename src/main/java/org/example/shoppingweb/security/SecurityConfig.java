package org.example.shoppingweb.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/login", "/signup", "/logout", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().permitAll() // hoặc `.authenticated()` nếu có route cần bảo vệ
                ).logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }
}
