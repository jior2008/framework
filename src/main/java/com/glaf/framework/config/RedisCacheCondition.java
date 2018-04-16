package com.glaf.framework.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisCacheCondition implements Condition {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		logger.debug("spring.config.location:" + System.getProperty("spring.config.location"));
		String host = context.getEnvironment().getProperty("spring.redis.host");
		if (StringUtils.isNotEmpty(host)) {
			return true;
		}
		logger.debug("redis not config!");
		return false;
	}

}
