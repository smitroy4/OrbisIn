package com.smit.orbisIn.postsService.feign;

import com.smit.orbisIn.postsService.context.ContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign interceptor that adds the userId from {@link ContextHolder} to outgoing Feign requests.
 */
@Configuration
public class FeignConfig {
    private static final String USER_ID_HEADER = "X-User-Id";

    @Bean
    public RequestInterceptor userIdRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Long userId = ContextHolder.getUserId();
                if (userId != null) {
                    template.header(USER_ID_HEADER, userId.toString());
                }
            }
        };
    }
}
