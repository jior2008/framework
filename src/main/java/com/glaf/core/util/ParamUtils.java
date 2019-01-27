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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ParamUtils {

	public static Object get(Map<String, Object> dataMap, String key) {
		if (dataMap == null || key == null) {
			return null;
		}
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		return value;
	}

	public static Boolean getBoolean(Map<String, Object> dataMap, String key) {
		Boolean result = null;
		if (dataMap == null || key == null) {
			return false;
		}
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null) {
			if (value instanceof Boolean) {
				result = (Boolean) value;
			} else {
				result = Boolean.valueOf(value.toString());
			}
		}
		return result;
	}

	public static Date getDate(Map<String, Object> dataMap, String key) {
		Date result = null;
		if (dataMap == null || key == null) {
			return null;
		}
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				result = DateUtils.toTimestamp(value.toString());
			} else if (value instanceof Date) {
				result = (Date) value;
			}
		}
		return result;
	}

	public static double getDouble(Map<String, Object> dataMap, String key) {
		double result;
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}

		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				String tmp = (String) value;
				tmp = StringTools.replace(tmp, ",", "");
				result = Double.parseDouble(tmp);
			} else if (value instanceof Integer) {
				Integer x = (Integer) value;
				result = x.doubleValue();
			} else if (value instanceof Long) {
				Long x = (Long) value;
				result = x.doubleValue();
			} else if (value instanceof Double) {
				Double d = (Double) value;
				result = d;
			} else {
				String tmp = value.toString();
				tmp = StringTools.replace(tmp, ",", "");
				result = Double.parseDouble(tmp);
			}
		} else {
			result = MapUtils.getDoubleValue(dataMap, key);
		}

		return result;
	}

	public static Double getDoubleValue(Map<String, Object> dataMap, String key) {
		Double result;
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				String tmp = (String) value;
				tmp = StringTools.replace(tmp, ",", "");
				result = Double.parseDouble(tmp);
			} else if (value instanceof Integer) {
				Integer x = (Integer) value;
				result = x.doubleValue();
			} else if (value instanceof Long) {
				Long x = (Long) value;
				result = x.doubleValue();
			} else if (value instanceof Double) {
				Double d = (Double) value;
				result = d;
			} else {
				String tmp = value.toString();
				tmp = StringTools.replace(tmp, ",", "");
				result = Double.parseDouble(tmp);
			}
		} else {
			result = MapUtils.getDouble(dataMap, key);
		}
		return result;
	}

	public static int getInt(Map<String, Object> dataMap, String key) {
		int result = 0;
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				String tmp = (String) value;
				tmp = StringTools.replace(tmp, ",", "");
				result = Integer.parseInt(tmp);
			} else if (value instanceof Integer) {
				Integer i = (Integer) value;
				result = i;
			} else if (value instanceof Long) {
				Long x = (Long) value;
				result = x.intValue();
			} else if (value instanceof Double) {
				Double x = (Double) value;
				result = x.intValue();
			} else {
				String tmp = value.toString();
				tmp = StringTools.replace(tmp, ",", "");
				result = Integer.parseInt(tmp);
			}
		}
		return result;
	}

	public static Integer getIntValue(Map<String, Object> dataMap, String key) {
		Integer result = null;
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				String tmp = (String) value;
				tmp = StringTools.replace(tmp, ",", "");
				result = Integer.parseInt(tmp);
			} else if (value instanceof Integer) {
				Integer i = (Integer) value;
				result = i;
			} else if (value instanceof Long) {
				Long l = (Long) value;
				result = l.intValue();
			} else if (value instanceof Double) {
				Double d = (Double) value;
				result = d.intValue();
			} else {
				String tmp = value.toString();
				tmp = StringTools.replace(tmp, ",", "");
				result = Integer.parseInt(tmp);
			}
		}
		return result;
	}

	public static long getLong(Map<String, Object> dataMap, String key) {
		long result = 0;
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				String tmp = (String) value;
				tmp = StringTools.replace(tmp, ",", "");
				result = Long.parseLong(tmp);
			} else if (value instanceof Integer) {
				Integer i = (Integer) value;
				result = (long) i.intValue();
			} else if (value instanceof Long) {
				Long l = (Long) value;
				result = l;
			} else if (value instanceof Double) {
				Double d = (Double) value;
				result = d.longValue();
			} else {
				String tmp = value.toString();
				tmp = StringTools.replace(tmp, ",", "");
				result = Long.parseLong(tmp);
			}
		}
		return result;
	}

	public static Long getLongValue(Map<String, Object> dataMap, String key) {
		Long result = null;
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				String tmp = (String) value;
				tmp = StringTools.replace(tmp, ",", "");
				result = Long.parseLong(tmp);
			} else if (value instanceof Integer) {
				Integer i = (Integer) value;
				result = (long) i.intValue();
			} else if (value instanceof Long) {
				Long l = (Long) value;
				result = l;
			} else if (value instanceof Double) {
				Double d = (Double) value;
				result = d.longValue();
			} else {
				String tmp = value.toString();
				tmp = StringTools.replace(tmp, ",", "");
				result = Long.parseLong(tmp);
			}
		}
		return result;
	}

	public static Object getObject(Map<String, Object> dataMap, String key) {
		if (dataMap == null || key == null) {
			return null;
		}
		Object result = dataMap.get(key);
		if (result == null) {
			result = dataMap.get(key.toLowerCase());
		}
		if (result == null) {
			result = dataMap.get(key.toUpperCase());
		}
		return result;
	}

	public static String getString(Map<String, Object> dataMap, String key) {
		String result = null;
		if (dataMap == null || key == null) {
			return null;
		}
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null) {
			if (value instanceof String) {
				result = (String) value;
			} else if (value instanceof Date) {
				result = DateUtils.getDate((Date) value);
			} else {
				result = value.toString();
			}
		}
		return result;
	}

	public static String getString(Map<String, Object> dataMap, String key, String defaultValue) {
		String result = defaultValue;
		if (dataMap == null || key == null) {
			return result;
		}
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null) {
			if (value instanceof String) {
				result = (String) value;
			} else {
				result = value.toString();
			}
		}
		return result;
	}

	public static Timestamp getTimestamp(Map<String, Object> dataMap, String key) {
		Timestamp result = null;
		if (dataMap == null || key == null) {
			return null;
		}
		Object value = dataMap.get(key);
		if (value == null) {
			value = dataMap.get(key.toLowerCase());
		}
		if (value == null) {
			value = dataMap.get(key.toUpperCase());
		}
		if (value != null && StringUtils.isNotEmpty(value.toString())) {
			if (value instanceof String) {
				result = DateUtils.toTimestamp(value.toString());
			} else if (value instanceof Date) {
				Date date = (Date) value;
				result = new Timestamp(date.getTime());
			}
		}
		return result;
	}

	public static boolean isNotEmpty(Map<String, Object> paramMap, String name) {
		if (paramMap != null && paramMap.get(name) != null) {
			Object obj = paramMap.get(name);
			if (obj instanceof Collection<?>) {
				Collection<?> rows = (Collection<?>) obj;
				return rows.size() > 0;
			}
		}
		return false;
	}
}