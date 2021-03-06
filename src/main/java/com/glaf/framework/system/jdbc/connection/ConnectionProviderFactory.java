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

package com.glaf.framework.system.jdbc.connection;

import com.glaf.core.util.ClassUtils;
import com.glaf.framework.system.config.DBConfiguration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConnectionProviderFactory {

	private static final Logger log = LoggerFactory.getLogger(ConnectionProviderFactory.class);

	private static final ConcurrentMap<String, ConnectionProvider> providerCache = new ConcurrentHashMap<String, ConnectionProvider>();

	private static final Set<String> SPECIAL_PROPERTIES;

	static {
		SPECIAL_PROPERTIES = new HashSet<String>();
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_URL);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_DRIVER);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_ISOLATION);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_DRIVER);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_USER);
	}

	private static boolean c3p0ConfigDefined(Properties properties) {
		for (Object o : properties.keySet()) {
			String property = (String) o;
			if (property.startsWith("c3p0")) {
				return true;
			}
		}
		return false;
	}

	private static boolean druidConfigDefined(Properties properties) {
		for (Object o : properties.keySet()) {
			String property = (String) o;
			if (property.startsWith("druid")) {
				return true;
			}
		}
		return false;
	}

	protected static void closeAndCreate(Properties properties) {
		String jdbcUrl = properties.getProperty(DBConfiguration.JDBC_URL);
		String user = properties.getProperty(DBConfiguration.JDBC_USER);
		String cacheKey = DigestUtils.md5Hex(jdbcUrl + user);
		ConnectionProvider provider = providerCache.get(cacheKey);
		if (provider != null) {
			provider.close();
		}
		provider = createProvider(properties);
		providerCache.put(cacheKey, provider);
	}

	private static boolean c3p0ProviderPresent() {
		try {
			ClassUtils.classForName("com.glaf.framework.system.jdbc.connection.C3P0ConnectionProvider");
		} catch (Exception e) {
			log.warn(
					"c3p0 properties is specificed, but could not find com.glaf.framework.system.jdbc.connection.C3P0ConnectionProvider from the classpath, "
							+ "these properties are going to be ignored.");
			return false;
		}
		return true;
	}

	private static boolean druidProviderPresent() {
		try {
			ClassUtils.classForName("com.glaf.framework.system.jdbc.connection.DruidConnectionProvider");
		} catch (Exception e) {
			log.warn(
					"druid properties is specificed, but could not find com.glaf.framework.system.jdbc.connection.DruidConnectionProvider from the classpath, "
							+ "these properties are going to be ignored.");
			return false;
		}
		return true;
	}

	public static ConnectionProvider createProvider(Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		String jdbcUrl = properties.getProperty(DBConfiguration.JDBC_URL);
		String user = properties.getProperty(DBConfiguration.JDBC_USER);
		String cacheKey = DigestUtils.md5Hex(jdbcUrl + user);
		if (providerCache.get(cacheKey) != null) {
			return providerCache.get(cacheKey);
		}
		ConnectionProvider provider = createMyProvider(properties);
		providerCache.put(cacheKey, provider);
		return provider;
	}

	private static ConnectionProvider createMyProvider(Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		log.debug("---------------------------ConnectionProvider create----------------");
		ConnectionProvider provider = null;
		String providerClass = properties.getProperty(DBConfiguration.JDBC_PROVIDER);
		if (providerClass != null) {
			provider = initializeConnectionProviderFromConfig(providerClass);
		} else if (c3p0ConfigDefined(properties) && c3p0ProviderPresent()) {
			provider = initializeConnectionProviderFromConfig(
					"com.glaf.framework.system.jdbc.connection.C3P0ConnectionProvider");
		} else if (druidConfigDefined(properties) && druidProviderPresent()) {
			provider = initializeConnectionProviderFromConfig(
					"com.glaf.framework.system.jdbc.connection.DruidConnectionProvider");
		}

		if (provider == null) {
			provider = initializeConnectionProviderFromConfig(
					"com.glaf.framework.system.jdbc.connection.DruidConnectionProvider");
		}

		if (StringUtils.equals(properties.getProperty(DBConfiguration.JDBC_DRIVER), "org.sqlite.JDBC")) {
			provider = initializeConnectionProviderFromConfig(
					"com.glaf.core.jdbc.connection.HikariCPConnectionProvider");
		}

		provider.configure(properties);
		log.debug("---------------------------ConnectionProvider end----------------");
		return provider;
	}

	public static ConnectionProvider createProvider(String systemName) {
		Properties props = DBConfiguration.getDataSourcePropertiesByName(systemName);
		// log.debug("ConnectionProvider@name=" + systemName);
		// log.debug("ConnectionProvider@props=" + props);
		return createProvider(props);
	}

	public static ConnectionProvider createProvider(String systemName, Properties properties) {
		return createProvider(properties);
	}

	static Properties getConnectionProperties(Properties properties) {
		Iterator<?> iter = properties.keySet().iterator();
		Properties result = new Properties();
		while (iter.hasNext()) {
			String prop = (String) iter.next();
			if (prop.startsWith(DBConfiguration.JDBC_DRIVER) && !SPECIAL_PROPERTIES.contains(prop)) {
				result.setProperty(prop.substring(DBConfiguration.JDBC_PREFIX.length() + 1),
						properties.getProperty(prop));
			}
		}
		String userName = properties.getProperty(DBConfiguration.JDBC_USER);
		if (userName != null) {
			result.setProperty("user", userName);
		}
		String pwd = properties.getProperty(DBConfiguration.JDBC_PASSWORD);
		if (pwd != null) {
			result.setProperty("password", pwd);
		}
		return result;
	}

	private static ConnectionProvider initializeConnectionProviderFromConfig(String providerClass) {
		ConnectionProvider connections;
		try {
			log.info("Initializing connection provider: " + providerClass);
			connections = (ConnectionProvider) ClassUtils.classForName(providerClass).newInstance();
		} catch (Exception ex) {
			log.error("Could not instantiate connection provider", ex);
			throw new RuntimeException("Could not instantiate connection provider: " + providerClass);
		}
		return connections;
	}

	private ConnectionProviderFactory() {

	}

}