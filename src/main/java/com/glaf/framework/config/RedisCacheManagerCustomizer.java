/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.glaf.framework.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * 配置缓存
 *
 */
@Configuration
@Conditional(RedisCacheCondition.class)
public class RedisCacheManagerCustomizer {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	public RedisCacheManager getRedisCacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheWriter cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer(loader);
		SerializationPair<Object> pair = SerializationPair.fromSerializer(jdkSerializer);

		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
		cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(3600));// 设置所有的超时时间

		// 设置单个缓存的超时时间
		Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();
		initialCacheConfigurations.put("user", cacheConfig.entryTtl(Duration.ofSeconds(600)));
		initialCacheConfigurations.put("menu", cacheConfig.entryTtl(Duration.ofSeconds(600)));
		initialCacheConfigurations.put("menuTree", cacheConfig.entryTtl(Duration.ofSeconds(600)));

		RedisCacheManager cacheManager = new RedisCacheManager(cacheWriter, cacheConfig, initialCacheConfigurations);
		logger.debug("--------------redis cache manager init finished-----------");
		return cacheManager;
	}
}
