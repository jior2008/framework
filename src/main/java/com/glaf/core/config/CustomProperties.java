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

import com.glaf.core.util.PropertiesUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomProperties {

	private static final Log logger = LogFactory.getLog(CustomProperties.class);

	private static final Properties properties = new Properties();

	private static final AtomicBoolean loading = new AtomicBoolean(false);

	static {
		try {
			reload();
		} catch (Exception ignored) {

		}
	}

	public static boolean eq(String key, String value) {
		if (key != null && value != null) {
			String x = properties.getProperty(key);
            return StringUtils.equals(value, x);
		}
		return false;
	}

	public static boolean getBoolean(String key) {
		if (hasObject(key)) {
			String value = properties.getProperty(key);
			return Boolean.valueOf(value);
		}
		return false;
	}

	public static double getDouble(String key) {
		if (hasObject(key)) {
			String value = properties.getProperty(key);
			return Double.parseDouble(value);
		}
		return 0;
	}

	public static int getInt(String key) {
		if (hasObject(key)) {
			String value = properties.getProperty(key);
			return Integer.parseInt(value);
		}
		return 0;
	}

	public static long getLong(String key) {
		if (hasObject(key)) {
			String value = properties.getProperty(key);
			return Long.parseLong(value);
		}
		return 0;
	}

	public static Properties getProperties() {
		Properties p = new Properties();
		Enumeration<?> e = properties.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = properties.getProperty(key);
			p.put(key, value);
		}
		return p;
	}

	public static String getString(String key) {
		if (hasObject(key)) {
			String value = properties.getProperty(key);
			if (value == null) {
				value = properties.getProperty(key.toUpperCase());
			}
			return value;
		}
		return null;
	}

	private static boolean hasObject(String key) {
		if (key == null) {
			return false;
		}
		String value = properties.getProperty(key);
        return value != null;
    }

	private static void reload() {
		if (!loading.get()) {
			InputStream inputStream = null;
			try {
				loading.set(true);
				String config = SystemProperties.getConfigRootPath() + "/conf/props/custom";
				logger.debug(config);
				File directory = new File(config);
				if (directory.exists() && directory.isDirectory()) {
					File[] filelist = directory.listFiles();
					if (filelist != null) {
						for (File file : filelist) {
							if (file.isFile() && file.getName().endsWith(".properties")) {
								logger.info("load properties:" + file.getAbsolutePath());
								inputStream = new FileInputStream(file);
								Properties props = PropertiesUtils.loadProperties(inputStream);
								if (props != null) {
									Enumeration<?> e = props.keys();
									while (e.hasMoreElements()) {
										String key = (String) e.nextElement();
										String value = props.getProperty(key);
										properties.setProperty(key, value);
										properties.setProperty(key.toLowerCase(), value);
										properties.setProperty(key.toUpperCase(), value);
									}
								}
								IOUtils.closeQuietly(inputStream);
							}
						}
					}
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				loading.set(false);
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

	private CustomProperties() {

	}

}