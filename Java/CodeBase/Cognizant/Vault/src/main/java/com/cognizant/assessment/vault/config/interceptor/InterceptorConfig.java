package com.cognizant.assessment.vault.config.interceptor;

import com.cognizant.assessment.vault.interceptor.CorrelationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CorrelationInterceptor())
                .addPathPatterns("/service/vault/getSecrets");
    }
}
