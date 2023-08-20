package cn.ichensw.bestapiadmin.config;

import cn.ichensw.bestapiadmin.aop.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/***
 * WebMvcConfig
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //拦截器对象
    @Resource
    private LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器对象，拦截所有路径，排除登录，不拦截登录
        registry.addInterceptor(loginCheckInterceptor)
                //.addPathPatterns("/**")//拦截所有请求，而且在放行以后还会被拦截回来
                //排除路径
                .excludePathPatterns(
                        "/user/login",
                        "/user/loginBySms",
                        "/user/register",
                        "/user/getCaptcha",
                        "/user/smsCaptcha",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources",
                        "/v2/api-docs"
                );
    }
}
