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

package com.glaf.core.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import java.awt.*;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Tools {
	private final static Log logger = LogFactory.getLog(Tools.class);

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getDataMap(Object target) {
		Map<String, Object> dataMap = new TreeMap<String, Object>();
		if (Map.class.isAssignableFrom(target.getClass())) {
			Map<String, Object> map = (Map<String, Object>) target;
			Set<Entry<String, Object>> entrySet = map.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (key != null && value != null) {
					dataMap.put(key.toString(), value);
				}
			}
		} else {
			PropertyDescriptor[] propertyDescriptor = BeanUtils
					.getPropertyDescriptors(target.getClass());
			for (PropertyDescriptor descriptor : propertyDescriptor) {
				String propertyName = descriptor.getName();
				if (propertyName.equalsIgnoreCase("class")) {
					continue;
				}
				try {
					Object value = PropertyUtils.getProperty(target,
							propertyName);
					dataMap.put(propertyName, value);
				} catch (Exception ignored) {

				}
			}
		}
		return dataMap;
	}

	private static Map<String, Class<?>> getPropertyMap(Class<?> clazz) {
		Map<String, Class<?>> dataMap = new java.util.HashMap<String, Class<?>>();
		PropertyDescriptor[] propertyDescriptor = BeanUtils
				.getPropertyDescriptors(clazz);
		for (PropertyDescriptor descriptor : propertyDescriptor) {
			String propertyName = descriptor.getName();
			if (propertyName.equalsIgnoreCase("class")) {
				continue;
			}
			dataMap.put(propertyName, descriptor.getPropertyType());
		}
		return dataMap;
	}

	public static Map<String, Class<?>> getPropertyMap(Object model) {
		return getPropertyMap(model.getClass());
	}

	private static Object getValue(Class<?> type, String propertyValue) {
		if (type == null || propertyValue == null
				|| propertyValue.trim().length() == 0) {
			return null;
		}
		Object value = null;
		try {
			if (type == String.class) {
				value = propertyValue;
			} else if ((type == Integer.class) || (type == int.class)) {
				if (propertyValue.indexOf(',') != -1) {
					propertyValue = propertyValue.replaceAll(",", "");
				}
				value = Integer.parseInt(propertyValue);
			} else if ((type == Long.class) || (type == long.class)) {
				if (propertyValue.indexOf(',') != -1) {
					propertyValue = propertyValue.replaceAll(",", "");
				}
				value = Long.parseLong(propertyValue);
			} else if ((type == Float.class) || (type == float.class)) {
				if (propertyValue.indexOf(',') != -1) {
					propertyValue = propertyValue.replaceAll(",", "");
				}
				value = Float.valueOf(propertyValue);
			} else if ((type == Double.class) || (type == double.class)) {
				if (propertyValue.indexOf(',') != -1) {
					propertyValue = propertyValue.replaceAll(",", "");
				}
				value = Double.parseDouble(propertyValue);
			} else if ((type == Boolean.class) || (type == boolean.class)) {
				value = Boolean.valueOf(propertyValue);
			} else if ((type == Character.class) || (type == char.class)) {
				value = propertyValue.charAt(0);
			} else if ((type == Short.class) || (type == short.class)) {
				if (propertyValue.indexOf(',') != -1) {
					propertyValue = propertyValue.replaceAll(",", "");
				}
				value = Short.valueOf(propertyValue);
			} else if ((type == Byte.class) || (type == byte.class)) {
				value = Byte.valueOf(propertyValue);
			} else if (type == java.util.Date.class) {
				value = DateUtils.toDate(propertyValue);
			} else if (type == java.sql.Date.class) {
				value = DateUtils.toDate(propertyValue);
			} else if (type == java.sql.Timestamp.class) {
				value = DateUtils.toDate(propertyValue);
			} else {
				value = propertyValue;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return value;
	}

	public static boolean isDatabaseField(String sourceString) {
		if (sourceString == null || sourceString.trim().length() < 2
				|| sourceString.trim().length() > 26) {
			return false;
		}
		char[] sourceChrs = sourceString.toCharArray();
		Character chr = sourceChrs[0];
		if (!((chr == 95)
				|| (65 <= chr && chr <= 90) || (97 <= chr && chr <= 122))) {
			return false;
		}
		for (int i = 1; i < sourceChrs.length; i++) {
			chr = sourceChrs[i];
			if (!((chr == 95)
					|| (47 <= chr && chr <= 57)
					|| (65 <= chr && chr <= 90) || (97 <= chr && chr <= 122))) {
				return false;
			}
		}
		return true;
	}

	public static String javaColorToCSSColor(Color paramColor) {
		StringBuilder localStringBuilder = new StringBuilder(30);
		localStringBuilder.append("rgb(");
		localStringBuilder.append(paramColor.getRed());
		localStringBuilder.append(',');
		localStringBuilder.append(paramColor.getGreen());
		localStringBuilder.append(',');
		localStringBuilder.append(paramColor.getBlue());
		localStringBuilder.append(')');
		return localStringBuilder.toString();
	}

	@SuppressWarnings("unchecked")
	public static void populate(Object model, Map<String, Object> dataMap) {
		if (model instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) model;
			Set<Entry<String, Object>> entrySet = map.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String key = entry.getKey();
				if (dataMap.containsKey(key)) {
					map.put(key, dataMap.get(key));
				}
			}
		} else {
			PropertyDescriptor[] propertyDescriptor = BeanUtils
					.getPropertyDescriptors(model.getClass());
			for (PropertyDescriptor descriptor : propertyDescriptor) {
				String propertyName = descriptor.getName();
				if (propertyName.equalsIgnoreCase("class")) {
					continue;
				}
				String value = null;
				Object val;
				Object object = dataMap.get(propertyName);
				if (object != null && object instanceof String) {
					value = (String) object;
				}
				try {
					Class<?> clazz = descriptor.getPropertyType();
					if (value != null) {
						val = getValue(clazz, value);
					} else {
						val = object;
					}
					if (val != null) {
						// PropertyUtils.setProperty(model, propertyName, x);
						ReflectUtils.setFieldValue(model, propertyName, val);
					}
				} catch (Exception ex) {
					logger.debug(ex);
				}
			}
		}
	}

	private Tools() {

	}

}