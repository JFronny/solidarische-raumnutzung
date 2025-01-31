package edu.kit.hci.soli.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.*;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final SoliOidcUserService userService;

    public WebSecurityConfig(SoliOidcUserService userService) {
        this.userService = userService;
    }

    // https://www.baeldung.com/spring-security-openid-connect
    // https://spring.io/guides/gs/securing-web
    // https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PersistentTokenRepository tokenRepository) throws Exception {
        http
                .authorizeHttpRequests(cfg -> cfg
                        .requestMatchers("/", "/{id:\\d+}", "/{id:\\d+}/", "/api/{id:\\d+}/events", "/api/holidays.ics", "/login/guest").permitAll()
                        .requestMatchers(
                                "/soli.css",
                                "/favicon.ico", "/favicon.svg", "/favicon_180x180.png", "/favicon_512x512.png", "/mask-icon.svg",
                                "/manifest.json",
                                "/robots.txt",
                                "/.well-known/security.txt"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(cfg -> cfg
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                        .userInfoEndpoint(userService::configure)
                )
                .formLogin(cfg -> cfg
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .rememberMe(cfg -> cfg
                        .key("remember-me")
                        .rememberMeCookieName("remember-me")
                        .rememberMeParameter("remember-me")
                        .tokenRepository(tokenRepository)
                        .tokenValiditySeconds(30 * 24 * 60 * 60)
                )
                .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
}
