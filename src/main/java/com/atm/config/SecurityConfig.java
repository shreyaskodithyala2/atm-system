package com.atm.config;

import com.atm.service.ATMUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private ATMUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain staffFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/staff/**")
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/staff/login", "/staff/login-error").permitAll()
                .requestMatchers("/staff/manager/**").hasRole("BANK_MANAGER")
                .requestMatchers("/staff/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/staff/login")
                .loginProcessingUrl("/staff/login")
                .successHandler(staffSuccessHandler())
                .failureUrl("/staff/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/staff/logout")
                .logoutSuccessUrl("/staff/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((req, res, e) -> res.sendRedirect("/staff/login?error=access"))
            );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain atmFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/atm/**", "/", "/h2-console/**", "/css/**", "/js/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler staffSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isManager = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BANK_MANAGER"));
            if (isManager) {
                response.sendRedirect("/staff/manager/dashboard");
            } else {
                response.sendRedirect("/staff/admin/dashboard");
            }
        };
    }
}
