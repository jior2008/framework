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

package com.glaf.core.cache;

import com.glaf.core.config.SystemProperties;
import com.glaf.core.util.ReflectUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CacheFactory {
    private static final Log logger = LogFactory.getLog(CacheFactory.class);
    private static final ConcurrentMap<String, CacheItem> cacheKeyMap = new ConcurrentHashMap<String, CacheItem>();
    private static final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();
    private static final List<CacheItem> items = new CopyOnWriteArrayList<CacheItem>();
    private static final List<String> regions = new CopyOnWriteArrayList<String>();
 
    public static void clear(final String region) {
        Cache cache = getCache();
        if (cache != null) {
            String _region = SystemProperties.getRegionName(region);
            try {
                cache.clear(_region);
            } catch (Throwable ignored) {
            }
        }
    }

    protected static void clearAll() {
        try {
            Cache cache = getCache();
            if (cache != null) {
                if (!items.isEmpty()) {
                    items.clear();
                }
                if (!regions.isEmpty()) {
                    for (String region : regions) {
                        cache.clear(region);
                    }
                }
            }
            logger.info("cache clear ok.");
        } catch (Throwable ignored) {
        } finally {
            items.clear();
        }
    }

    private static Cache getCache() {
        Cache cache = null;
        String provider = CacheProperties.getString("cache_provider");
        if (provider != null) {
            cache = cacheMap.get(provider);
        }
        if (cache == null) {
            if (StringUtils.equals(provider, "caffeine")) {
                String cacheClass = "com.glaf.core.cache.caffeine.CaffeineCache";
                cache = (Cache) ReflectUtils.instantiate(cacheClass);
            } else if (StringUtils.equals(provider, "ehcache")) {
                String cacheClass = "com.glaf.core.cache.ehcache.EHCacheImpl";
                cache = (Cache) ReflectUtils.instantiate(cacheClass);
            } else if (StringUtils.equals(provider, "guava")) {
                String cacheClass = "com.glaf.core.cache.guava.GuavaCache";
                cache = (Cache) ReflectUtils.instantiate(cacheClass);
            } else if (StringUtils.equals(provider, "j2cache")) {
                String cacheClass = "com.glaf.core.cache.j2cache.J2CacheImpl";
                cache = (Cache) ReflectUtils.instantiate(cacheClass);
            } else {
                provider = "caffeine";
                String cacheClass = "com.glaf.core.cache.caffeine.CaffeineCache";
                cache = (Cache) ReflectUtils.instantiate(cacheClass);
            }
            if (cache != null && provider != null) {
                logger.debug("cache provider:" + provider);
                logger.debug("cache provider class:" + cache.getClass().getName());
                cacheMap.put(provider, cache);
            }
        }
        return cache;
    }

    public static List<CacheItem> getCacheItems() {
        return items;
    }

    public static String getString(final String region, final String key) {
        Cache cache = getCache();
        if (cache != null) {
            String _region = SystemProperties.getRegionName(region);
            String cacheKey = _region + "_" + key;
            cacheKey = DigestUtils.md5Hex(cacheKey.getBytes());
            Object value = cache.get(_region, cacheKey);
            if (value != null) {
                String val = value.toString();
                val = new String(com.glaf.core.util.Hex.hex2byte(val), StandardCharsets.UTF_8);
                return val;
            }
        }
        return null;
    }

    public static void put(final String region, final String key, final String value) {
        put(region, key, value, 1800);
    }

    private static void put(final String region, final String key, final String value, long timeToLiveInSeconds) {
        try {
            Cache cache = getCache();
            if (cache != null && key != null && value != null) {
                // remove(region, key);
                String _region = SystemProperties.getRegionName(region);
                String cacheKey = _region + "_" + key;
                cacheKey = DigestUtils.md5Hex(cacheKey.getBytes());
                int limitSize = 5120000;// 5MB
                if (value.length() < limitSize) {
                    String val = com.glaf.core.util.Hex.byte2hex(value.getBytes(StandardCharsets.UTF_8));
                    cache.put(_region, cacheKey, val, timeToLiveInSeconds);
                    logger.debug("put object into cache.");
                    if (!regions.contains(_region)) {
                        regions.add(_region);
                    }
                    CacheItem item = new CacheItem();
                    item.setRegion(region);
                    item.setName(key);
                    item.setKey(cacheKey);
                    item.setLastModified(System.currentTimeMillis());
                    item.setTimeToLiveInSeconds((long) 1800);
                    item.setSize(value.length());
                    items.add(item);
                    cacheKeyMap.put(cacheKey, item);
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void remove(final String region, final String key) {
        try {
            Cache cache = getCache();
            if (cache != null) {
                String _region = SystemProperties.getRegionName(region);
                String cacheKey = _region + "_" + key;
                cacheKey = DigestUtils.md5Hex(cacheKey.getBytes());
                cache.remove(_region, cacheKey);
                logger.debug("remove object from cache.");
                CacheItem item = cacheKeyMap.get(cacheKey);
                if (item != null) {
                    items.remove(item);
                }
                cacheKeyMap.remove(cacheKey);
            }
        } catch (Throwable ignored) {
        }
    }

    protected static void shutdown() {
        try {
            Cache cache = getCache();
            if (cache != null) {
                if (!items.isEmpty()) {
                    items.clear();
                }
                if (!regions.isEmpty()) {
                    for (String region : regions) {
                        cache.clear(region);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

}
