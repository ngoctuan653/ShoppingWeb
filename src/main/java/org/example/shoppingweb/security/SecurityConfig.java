package org.example.shoppingweb.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomLoginSuccessHandler loginSuccessHandler;

    @Autowired
    private CustomOAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/index", "/", "/shop", "/home", "/signup", "/forgot-password", "/error",
                                "/doLogin", "/oauth2/**", "/css/**", "/js/**",
                                "/about", "/contact", "/products/image/**",
                                "/products/**", "/search", "/images/**", "/{productId}/sizes",
                                "/verify-reset-code", "/reset-password", "/resend-code"
                        ).permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("text/plain;charset=UTF-8");
                                response.getWriter().write("Please login");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedPage("/403")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/oauth2/success", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("remember-me") // Tên checkbox trong form
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 ngày
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me") // xoá cookie remember-me khi logout
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
