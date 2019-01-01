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

package com.glaf.core.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CaffeineCache implements com.glaf.core.cache.Cache {
	protected static final Log logger = LogFactory.getLog(CaffeineCache.class);

	private static final ConcurrentMap<String, Cache<Object, Object>> cacheConcurrentMap = new ConcurrentHashMap<String, Cache<Object, Object>>();

	private static final AtomicBoolean running = new AtomicBoolean(false);

	private Cache<Object, Object> cache;

	private int cacheSize = 50000;

	private int expireMinutes = 10;

	private CaffeineCache() {

	}

	public void clear() {
		getCache().invalidateAll();
		getCache().cleanUp();

        for (String region : cacheConcurrentMap.keySet()) {
            getCache(region).invalidateAll();
            getCache(region).cleanUp();
        }
		cacheConcurrentMap.clear();
	}

	public void clear(String region) {
		getCache(region).invalidateAll();
		getCache(region).cleanUp();
		cacheConcurrentMap.remove(region);
	}

	public Object get(String key) {
		return getCache().getIfPresent(key);
	}

	@Override
	public String get(String region, String key) {
		Object value = getCache(region).getIfPresent(key);
		if (value != null) {
			if (value instanceof String) {
				return (String) value;
			}
		}
		return null;
	}

	private Cache<Object, Object> getCache() {
		if (cache == null) {
			if (!running.get()) {
				try {
					running.set(true);
					if (cache == null) {
						cache = Caffeine.newBuilder().maximumSize(getCacheSize())
								.expireAfterWrite(getExpireMinutes(), TimeUnit.MINUTES).build();
					}
				} finally {
					running.set(false);
				}
			}
		}
		return cache;
	}

	private Cache<Object, Object> getCache(String region) {
		Cache<Object, Object> regionCache = cacheConcurrentMap.get(region);
		if (regionCache == null) {
			if (!running.get()) try {
				running.set(true);
				regionCache = Caffeine.newBuilder().maximumSize(getCacheSize())
						.expireAfterWrite(getExpireMinutes(), TimeUnit.MINUTES).build();
				cacheConcurrentMap.put(region, regionCache);
			} finally {
				running.set(false);
			}
		}
		return regionCache;
	}

	private int getCacheSize() {
		return cacheSize;
	}

	private int getExpireMinutes() {
		return expireMinutes;
	}

	public void put(String key, Object value) {
		Cache<Object, Object> cache = this.getCache();
		cache.invalidate(key);
		cache.put(key, value);
	}

	public void put(String region, String key, Object value) {
		Cache<Object, Object> cache = this.getCache(region);
		cache.invalidate(key);
		cache.put(key, value);
	}

	@Override
	public void put(String region, String key, String value) {
		Cache<Object, Object> cache = this.getCache(region);
		cache.invalidate(key);
		cache.put(key, value);
	}

	@Override
	public void put(String region, String key, String value, long timeToLiveInSeconds) {
		Cache<Object, Object> cache = this.getCache(region);
		cache.invalidate(key);
		cache.put(key, value);
	}

	public void remove(String key) {
		getCache().invalidate(key);
	}

	public void remove(String region, String key) {
		getCache(region).invalidate(key);
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public void setExpireMinutes(int expireMinutes) {
		this.expireMinutes = expireMinutes;
	}

	public void shutdown() {
		getCache().invalidateAll();
		getCache().cleanUp();
        for (String region : cacheConcurrentMap.keySet()) {
            getCache(region).invalidateAll();
            getCache(region).cleanUp();
        }
		cacheConcurrentMap.clear();
	}
}
