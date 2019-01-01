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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持一二级缓存,使得性能逆天快.默认不开启
 * 
 */

// @Configuration
// @ConditionalOnProperty(name="springext.cache.enabled", havingValue="true"
// ,matchIfMissing=false)
public class CacheConfig {

	// 定义一个redis 的频道，默认叫cache，用于pub/sub
	@Value("${springext.cache.redis.topic:cache}")
	String topicName;

	@SuppressWarnings("rawtypes")
	@Bean
	public TowLevelCacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(redisTemplate.getConnectionFactory());
		SerializationPair pair = SerializationPair
				.fromSerializer(new JdkSerializationRedisSerializer(this.getClass().getClassLoader()));
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
        return new TowLevelCacheManager(redisTemplate, writer, config);
	}

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic(topicName));
		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(final TowLevelCacheManager cacheManager) {
		return new MessageListenerAdapter(new MessageListener() {

			public void onMessage(Message message, byte[] pattern) {
				byte[] bs = message.getChannel();
				try {
					// Sub 一个消息，通知缓存管理器
					String type = new String(bs, StandardCharsets.UTF_8);
					cacheManager.receiver(type);
				} catch (Exception e) {
					e.printStackTrace();
					// 不可能出错，忽略
				}
			}

		});
	}

	@SuppressWarnings("rawtypes")
	class TowLevelCacheManager extends RedisCacheManager {

		final RedisTemplate redisTemplate;

		TowLevelCacheManager(RedisTemplate redisTemplate, RedisCacheWriter cacheWriter,
							 RedisCacheConfiguration defaultCacheConfiguration) {
			super(cacheWriter, defaultCacheConfiguration);
			this.redisTemplate = redisTemplate;
		}

		// 使用RedisAndLocalCache代替Spring Boot自带的RedisCache
		@Override
		protected Cache decorateCache(Cache cache) {
			return new RedisAndLocalCache(this, (RedisCache) cache);
		}

		// 通过其他分布式节点，缓存改变
		void publishMessage(String cacheName) {
			this.redisTemplate.convertAndSend(topicName, cacheName);
		}

		// 接受一个消息清空本地缓存
		void receiver(String name) {
			RedisAndLocalCache cache = ((RedisAndLocalCache) this.getCache(name));
			if (cache != null) {
				cache.clearLocal();
			}
		}

	}

	class RedisAndLocalCache implements Cache {
		// 本地缓存提供
		final ConcurrentHashMap<Object, Object> local = new ConcurrentHashMap<Object, Object>();
		final RedisCache redisCache;
		final TowLevelCacheManager cacheManager;

		RedisAndLocalCache(TowLevelCacheManager cacheManager, RedisCache redisCache) {
			this.redisCache = redisCache;
			this.cacheManager = cacheManager;
		}

		@Override
		public String getName() {
			return redisCache.getName();
		}

		@Override
		public Object getNativeCache() {
			return redisCache.getNativeCache();
		}

		@Override
		public ValueWrapper get(Object key) {
			ValueWrapper wrapper = (ValueWrapper) local.get(key);
			if (wrapper != null) {
				return wrapper;
			} else {
				// 二级缓存取
				wrapper = redisCache.get(key);
				if (wrapper != null) {
					local.put(key, wrapper);
				}
				return wrapper;
			}
		}

		@Override
		public <T> T get(Object key, Class<T> type) {
			return redisCache.get(key, type);
		}

		@Override
		public <T> T get(Object key, Callable<T> valueLoader) {
			return redisCache.get(key, valueLoader);
		}

		@Override
		public void put(Object key, Object value) {
			redisCache.put(key, value);
			clearOtherJVM();
		}

		@Override
		public ValueWrapper putIfAbsent(Object key, Object value) {
			ValueWrapper v = redisCache.putIfAbsent(key, value);
			clearOtherJVM();
			return v;
		}

		@Override
		public void evict(Object key) {
			redisCache.evict(key);
			clearOtherJVM();
		}

		@Override
		public void clear() {
			redisCache.clear();
		}

		void clearLocal() {
			this.local.clear();
		}

		void clearOtherJVM() {
			cacheManager.publishMessage(redisCache.getName());
		}

	}

}