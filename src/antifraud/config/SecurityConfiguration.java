package antifraud.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .mvcMatchers("/actuator/shutdown").permitAll()
                .antMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                .mvcMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction/**").hasRole("MERCHANT")
                .mvcMatchers(HttpMethod.PUT, "/api/auth/role/**").hasRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.PUT, "/api/auth/access/**").hasRole("ADMINISTRATOR")
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }
}
