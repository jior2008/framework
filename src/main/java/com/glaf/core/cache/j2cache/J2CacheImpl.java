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

package com.glaf.core.cache.j2cache;

import com.glaf.core.cache.Cache;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import net.oschina.j2cache.J2Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class J2CacheImpl implements Cache {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final CacheChannel cacheChannel = J2Cache.getChannel();

	private static final List<String> regions = new CopyOnWriteArrayList<String>();

	@Override
	public void clear(String region) {
		try {
			cacheChannel.clear(region);
		} catch (Exception ignored) {
		}
	}

	@Override
	public String get(String region, String key) {
		CacheObject object = null;
		try {
			object = cacheChannel.get(region, key);
		} catch (Exception ignored) {
		}
		if (object != null) {
			logger.debug("get object from j2cache.");
			Object value = object.getValue();
			if (value != null) {
				// logger.debug("value class:" + value.getClass().getName());
				if (value instanceof String) {
					String str = (String) value;
					return new String(com.glaf.core.util.Hex.hex2byte(str), StandardCharsets.UTF_8);
				} else if (value instanceof byte[]) {
					byte[] bytes = (byte[]) value;
					return new String(bytes, StandardCharsets.UTF_8);
				} else {
					return object.asString();
				}
			} else {
				logger.debug("value is null.");
				return object.asString();
			}
		}
		return null;
	}

	@Override
	public void put(String region, String key, String value) {
		this.put(region, key, value, 1800L);
	}

	@Override
	public void put(String region, String key, String value, long timeToLiveInSeconds) {
		if (value != null) {
			if (timeToLiveInSeconds == 0) {
				timeToLiveInSeconds = 1800L;
			}
			try {
				cacheChannel.set(region, key, com.glaf.core.util.Hex.byte2hex(value.getBytes(StandardCharsets.UTF_8)),
						timeToLiveInSeconds);
				if (!regions.contains(region)) {
					regions.add(region);
				}
				logger.debug("put object into j2cache.");
			} catch (Exception ex) {
				// logger.error("put j2cache error", ex);
				try {
					cacheChannel.set(region, key, com.glaf.core.util.Hex.byte2hex(value.getBytes()),
							timeToLiveInSeconds);
				} catch (Exception ignored) {
				}
			}
		}
	}

	@Override
	public void remove(String region, String key) {
		try {
			cacheChannel.evict(region, key);
		} catch (Exception ignored) {
		}
	}

	@Override
	public void shutdown() {
		for (String region : regions) {
			try {
				cacheChannel.clear(region);
			} catch (Exception ignored) {
			}
		}
		cacheChannel.close();
	}

}
