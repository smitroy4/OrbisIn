package com.smit.orbisIn.uploaderService.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Only matches when storage credentials are actually configured, so the
 * service can still boot (health reports storage as down) instead of
 * crashing the whole application context at startup.
 */
public class StorageCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String accessKey = context.getEnvironment().getProperty("app.storage.access-key", "");
        String secretKey = context.getEnvironment().getProperty("app.storage.secret-key", "");
        return StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey);
    }
}
