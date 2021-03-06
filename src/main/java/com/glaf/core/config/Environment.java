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

package com.glaf.core.config;

import com.glaf.core.security.LoginContext;
import io.netty.util.concurrent.FastThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Environment {
	private static final Log logger = LogFactory.getLog(Environment.class);

	private static final String CURRENT_SYSTEM_NAME = "CURRENT_SYSTEM_NAME";

	private static final String CURRENT_USER = "CURRENT_USER";

	public static final String DEFAULT_SYSTEM_NAME = "default";

	public static final String POSTGRESQL = "postgresql";

	private static String databaseType;

	private static final ConcurrentMap<String, Properties> systemProperties = new ConcurrentHashMap<String, Properties>();

	private static final FastThreadLocal<LoginContext> threadLocalContexts = new FastThreadLocal<LoginContext>();

	private static final FastThreadLocal<Map<String, String>> threadLocalVaribles = new FastThreadLocal<Map<String, String>>();

	private static final FastThreadLocal<String> systemNameThreadLocal = new FastThreadLocal<String>();

	public static void clear() {
		threadLocalContexts.remove();
		threadLocalVaribles.remove();
		systemNameThreadLocal.remove();
	}

	public static String getCurrentSystemName() {
		if (systemNameThreadLocal.get() != null) {
			return systemNameThreadLocal.get();
		}
		return DEFAULT_SYSTEM_NAME;
	}

	public static Properties getCurrentSystemProperties() {
		String dsName = getCurrentSystemName();
		return systemProperties.get(dsName);
	}

	public static String getCurrentUser() {
		if (threadLocalVaribles.get() != null) {
			return threadLocalVaribles.get().get(CURRENT_USER);
		}
		return null;
	}

	public static String getDatabaseType() {
		return databaseType;
	}

	
	public static LoginContext getLoginContext() {
		if (threadLocalContexts.get() != null) {
			return threadLocalContexts.get();
		}
		return null;
	}

	public static Map<String, Properties> getSystemProperties() {
		return systemProperties;
	}

	public static Properties getSystemPropertiesByName(String name) {
		Properties props = systemProperties.get(name);
		Properties p = new Properties();
		Enumeration<?> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			p.put(key, value);
		}
		return p;
	}

	public static String getThreadLocalProperty(String key) {
		if (threadLocalVaribles.get() != null) {
			return threadLocalVaribles.get().get(key);
		}
		return null;
	}

	public static String getThreadLocalProperty(String key, String defaultValue) {
		if (threadLocalVaribles.get() != null) {
			return threadLocalVaribles.get().get(key);
		}
		return defaultValue;
	}

	public static void removeCurrentSystemName() {
		systemNameThreadLocal.set(null);
		systemNameThreadLocal.remove();
	}

	public static void setCurrentSystemName(String value) {
		systemNameThreadLocal.set(value);
		logger.debug("->systemName:" + Environment.getCurrentSystemName());
	}

	public static void setCurrentUser(String actorId) {
		Map<String, String> dataMap = threadLocalVaribles.get();
		if (dataMap == null) {
			dataMap = new java.util.HashMap<String, String>();
			threadLocalVaribles.set(dataMap);
		}
		dataMap.put(CURRENT_USER, actorId);
	}

	public static void setDatabaseType(String databaseTypeX) {
		databaseType = databaseTypeX;
	}

	public static void setLoginContext(LoginContext loginContext) {
		if (loginContext != null) {
			threadLocalContexts.set(loginContext);
		}
	}

	public static void setThreadLocalProperty(String key, String value) {
		Map<String, String> dataMap = threadLocalVaribles.get();
		if (dataMap == null) {
			dataMap = new java.util.HashMap<String, String>();
			threadLocalVaribles.set(dataMap);
		}
		if (!(StringUtils.equals(key, CURRENT_SYSTEM_NAME) || StringUtils.equals(key, CURRENT_USER))) {
			dataMap.put(key, value);
		}
	}

}