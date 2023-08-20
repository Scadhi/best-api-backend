package cn.ichensw.bestapiadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/***
 * MyCorsFilterConfig
 **/
@Configuration
public class MyCorsFilterConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 当allowCredentials 为 true 时，allowOrigins 不能包含特殊值“*”，
        // 因为无法在“Access-Control-Allow-Origin”响应标头上设置该值。
        // 要允许一组源的凭据，请明确列出它们或考虑改用“allowedOriginPatterns”。
        //config.addAllowedOrigin("*");
        config.addAllowedOriginPattern("*");
        // 允许发送 Cookie
        config.setAllowCredentials(true);
        //config.addAllowedMethod("GET", "POST", "PUT", "DELETE", "OPTIONS");
        config.addAllowedMethod("*");
        //表示访问请求中允许携带哪些Header信息，如：Accept、Accept-Language、Content-Language、Content-Type
        config.addAllowedHeader("*");
        config.setMaxAge(3600L);
        //暴露哪些头部信息(因为跨域访问默认不能获取全部头部信息)
        config.addExposedHeader("*");
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        // 覆盖所有请求
        configSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configSource);
    }
}
