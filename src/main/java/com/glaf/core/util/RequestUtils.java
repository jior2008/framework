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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.security.IdentityFactory;
import com.glaf.core.security.LoginContext;
import com.glaf.framework.system.config.SystemConfig;
import com.glaf.framework.system.security.AESUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RequestUtils {
	private static final int COOKIE_LIVING_SECONDS = 3600 * 8;

	private final static Log logger = LogFactory.getLog(RequestUtils.class);

	private static void append(Object key, Object value, StringBuilder queryString, String ampersand) {
		if (queryString.length() > 0) {
			queryString.append(ampersand);
		}
		try {
			queryString.append(URLEncoder.encode(key.toString(), "UTF-8"));
			queryString.append('=');
			queryString.append(URLEncoder.encode(value.toString(), "UTF-8"));
		} catch (IOException ignored) {
		}
	}

	private static StringBuilder createQueryStringFromMap(Map<String, Object> m, String ampersand) {
		StringBuilder aReturn = new StringBuilder();
		Set<Entry<String, Object>> entrySet = m.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				append(key, "", aReturn, ampersand);
			} else if (value instanceof String) {
				append(key, value, aReturn, ampersand);
			} else if (value instanceof String[]) {
				String[] aValues = (String[]) value;
				for (String aValue : aValues) {
					append(key, aValue, aReturn, ampersand);
				}
			} else {
				append(key, value, aReturn, ampersand);
			}
		}
		return aReturn;
	}

	/**
	 * 解码字符串，本方法用于临时数据的解码 jdk1.8必须升级JCE包，否则加解密不成功！
	 * 
	 * @param str
	 * @return
	 */
	private static String decodeString(String str) {
		try {
			// logger.debug("AES解密前:" + str);
			byte[] key = AESUtils.initAndLoadKey();
			byte[] data = AESUtils.decryptECB(key, Hex.hex2byte(str));
			String tmp = new String(Base64.decodeBase64(data), StandardCharsets.UTF_8);
			String salt = DigestUtils.md5Hex(SystemConfig.getToken());
			tmp = StringUtils.replace(tmp, salt, "");
			// logger.debug("AES解密后:" + tmp);
			return tmp;
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.error(ex);
		}
		return str;
	}

	/**
	 * 解码字符串，本方法用于临时数据的解码 jdk1.8必须升级JCE包，否则加解密不成功！
	 * 
	 * @param str
	 * @param token
	 * @return
	 */
	public static String decodeString(String str, String token) {
		try {
			// logger.debug("AES解密前:" + str);
			byte[] key = AESUtils.initAndLoadKey();
			byte[] data = AESUtils.decryptECB(key, Hex.hex2byte(str));
			String tmp = new String(Base64.decodeBase64(data), StandardCharsets.UTF_8);
			String salt = DigestUtils.md5Hex(token);
			tmp = StringUtils.replace(tmp, salt, "");
			// logger.debug("AES解密后:" + tmp);
			return tmp;
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.error(ex);
		}
		return str;
	}

	/**
	 * 解码字符串，本方法用于临时数据的解码
	 * 
	 * @param str
	 * @return
	 */
	public static String decodeURL(String str) {
		return decodeString(str);
	}

	private static Map<String, String> decodeValues(String ip, String value) {
		Map<String, String> cookieMap = new java.util.HashMap<String, String>();
		if (StringUtils.isNotEmpty(value)) {
			String c_x = decodeString(value);
			c_x = StringUtils.replace(c_x, DigestUtils.md5Hex(ip + SystemConfig.getIntToken()), "");
			JSONObject jsonObject = null;
			try {
				jsonObject = JSON.parseObject(c_x);
			} catch (Exception ex) {
				// Ignore Exception
			}
			if (jsonObject != null) {
				for (Entry<String, Object> entry : jsonObject.entrySet()) {
					String key = entry.getKey();
					Object val = entry.getValue();
					if (val != null) {
						cookieMap.put(key, val.toString());
					}
				}
			}
		}
		return cookieMap;
	}

	/**
	 * 编码字符串，本方法用于临时数据的编码，永久保存的加密数据用加密方法 jdk1.8必须升级JCE包，否则加解密不成功！
	 * 
	 * @param str
	 * @return
	 */
	public static String encodeString(String str) {
		try {
			String salt = DigestUtils.md5Hex(SystemConfig.getToken());
			str = str + salt;
			// logger.error("待加密数据:" + str);
			byte[] key = AESUtils.initAndLoadKey();
			byte[] bytes = Base64.encodeBase64(str.getBytes(StandardCharsets.UTF_8));
			byte[] data = AESUtils.encryptECB(key, bytes);
			str = Hex.byte2hex(data);
			// logger.debug("AES加密后:" + str);
			return str;
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.error(ex);
		}
		return str;
	}

	/**
	 * 编码字符串，本方法用于临时数据的编码，永久保存的加密数据用加密方法 jdk1.8必须升级JCE包，否则加解密不成功！
	 * 
	 * @param str
	 * @param token
	 * @return
	 */
	public static String encodeString(String str, String token) {
		try {
			String salt = DigestUtils.md5Hex(token);
			str = str + salt;
			byte[] key = AESUtils.initAndLoadKey();
			byte[] bytes = Base64.encodeBase64(str.getBytes(StandardCharsets.UTF_8));
			byte[] data = AESUtils.encryptECB(key, bytes);
			str = Hex.byte2hex(data);
			// logger.debug("AES加密后:" + str);
			return str;
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.error(ex);
		}
		return str;
	}

	/**
	 * 编码字符串，本方法用于临时数据的编码，永久保存的加密数据用加密方法
	 * 
	 * @param str
	 * @return
	 */
	public static String encodeURL(String str) {
		return encodeString(str);
	}

	private static String encodeValues(String ip, String systemName, String actorId) {
		JSONObject rootJson = new JSONObject();
		rootJson.put(Constants.LOGIN_IP, ip);
		rootJson.put(Constants.LOGIN_ACTORID, actorId);
		if (systemName != null) {
			rootJson.put(Constants.SYSTEM_NAME, systemName);
		}
		rootJson.put(Constants.TS, String.valueOf(System.currentTimeMillis()));
		String text = rootJson.toJSONString();
		text = DigestUtils.md5Hex(ip + SystemConfig.getIntToken()) + text;
		text = encodeString(text);
		return text;
	}

	private static String getActorId(HttpServletRequest request) {
		String actorId = null;
		String ip = getIPAddress(request);
		ip = DigestUtils.md5Hex(ip + SystemConfig.getIntToken());
		HttpSession session = request.getSession(false);
		// logger.debug("session is null --->" + (session == null));
		if (session != null) {
			String value = (String) session.getAttribute(Constants.LOGIN_INFO);
			// logger.debug("session login_info value --->" + (session ==
			// null));
			Map<String, String> cookieMap = decodeValues(ip, value);
			if (StringUtils.equals(cookieMap.get(Constants.LOGIN_IP), ip)) {
				actorId = cookieMap.get(Constants.LOGIN_ACTORID);
				logger.debug("#actorId=" + actorId);
			}
		}

		if (actorId == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equals(cookie.getName(), Constants.COOKIE_NAME)) {
						String value = cookie.getValue();
						// logger.debug("cookie's value --->" + value);
						Map<String, String> cookieMap = decodeValues(ip, value);
						// logger.debug("#cookieMap=" + cookieMap);
						if (StringUtils.equals(cookieMap.get(Constants.LOGIN_IP), ip)) {
							String time = cookieMap.get(Constants.TS);
							long now = System.currentTimeMillis();
							if (StringUtils.isNumeric(time)
									&& (now - Long.parseLong(time)) < COOKIE_LIVING_SECONDS * 1000) {
								actorId = cookieMap.get(Constants.LOGIN_ACTORID);
								break;
							}
						}
					}
				}
			}
		}

		logger.debug("@actorId=" + actorId);

		return actorId;
	}

	public static String getAttribute(HttpServletRequest request, String name) {
		Object value = request.getAttribute(name);
		if (value != null) {
			return value.toString();
		}
		return "";
	}

	/**
	 * 从request中获取boolean参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static boolean getBoolean(HttpServletRequest request, String paramName) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue)) {
			if ("on".equalsIgnoreCase(paramValue)) {
				return true;
			}
			if ("ok".equalsIgnoreCase(paramValue)) {
				return true;
			}
			if ("true".equalsIgnoreCase(paramValue)) {
				return true;
			}
            return "1".equalsIgnoreCase(paramValue);
		}
		return false;
	}

	/**
	 * 获取字段值
	 * 
	 * @param request
	 *            HttpServletRequest request对象
	 * @param param
	 *            String 参数
	 * @param value
	 *            String 比较值
	 * @return boolean 和比较值相同则返回true，否则返回false
	 */
	public static boolean getBooleanParameter(HttpServletRequest request, String param, String value) {
		String temp = getParameter(request, param);
        return temp != null && temp.equals(value);
    }

	public static String getCookieValue(HttpServletRequest request, String name) {
		String value = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equals(cookie.getName(), name)) {
					value = cookie.getValue();
				}
			}
		}
		return value;
	}

	private static String getCurrentSystem(HttpServletRequest request) {
		String currentSystem = null;
		String paramValue = request.getParameter(Constants.SYSTEM_NAME);
		if (StringUtils.isNotEmpty(paramValue)) {
			return paramValue;
		}
		String ip = getIPAddress(request);
		ip = DigestUtils.md5Hex(ip + SystemConfig.getIntToken());
		HttpSession session = request.getSession(false);
		if (session != null) {
			String value = (String) session.getAttribute(Constants.LOGIN_INFO);
			Map<String, String> cookieMap = decodeValues(ip, value);
			if (StringUtils.equals(cookieMap.get(Constants.LOGIN_IP), ip)) {
				currentSystem = cookieMap.get(Constants.SYSTEM_NAME);
			}
		}

		if (currentSystem == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equals(cookie.getName(), Constants.COOKIE_NAME)) {
						String value = cookie.getValue();
						Map<String, String> cookieMap = decodeValues(ip, value);
						if (StringUtils.equals(cookieMap.get(Constants.LOGIN_IP), ip)) {
							String time = cookieMap.get(Constants.TS);
							long now = System.currentTimeMillis();
							if (StringUtils.isNumeric(time)
									&& (now - Long.parseLong(time)) < COOKIE_LIVING_SECONDS * 1000) {
								currentSystem = cookieMap.get(Constants.SYSTEM_NAME);
								break;
							}
						}
					}
				}
			}
		}

		return currentSystem;
	}

	/**
	 * 从request中获取date参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static java.util.Date getDate(HttpServletRequest request, String paramName) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue)) {
			try {
				return DateUtils.toDate(paramValue);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	/**
	 * 从request中获取double参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static double getDouble(HttpServletRequest request, String paramName) {
		return getDouble(request, paramName, 0.0);
	}

	/**
	 * 从request中获取double参数
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	private static double getDouble(HttpServletRequest request, String paramName, double defaultValue) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue) && StringUtils.isNumeric(paramValue)) {
			return Double.parseDouble(paramValue);
		} else {
			if (StringUtils.isNotEmpty(paramValue)) {
				try {
					return Double.parseDouble(paramValue);
				} catch (Exception ignored) {
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 从request中获取date参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static java.util.Date getEndDate(HttpServletRequest request, String paramName) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue)) {
			if (!paramValue.endsWith(" 23:59:59")) {
				paramValue = paramValue + " 23:59:59";
			}
			try {
				return DateUtils.toDate(paramValue);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static String getHomeTheme(HttpServletRequest request) {
		String homeTheme = "default";
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equals(cookie.getName(), Constants.HOME_THEME_COOKIE)) {
					if (cookie.getValue() != null) {
						homeTheme = cookie.getValue();
					}
				}
			}
		}
		if (StringUtils.isNotEmpty(request.getParameter("homeTheme"))) {
			homeTheme = request.getParameter("homeTheme");
		}
		return homeTheme;
	}

	/**
	 * 从request中获取int参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static int getInt(HttpServletRequest request, String paramName) {
		return getInt(request, paramName, 0);
	}

	/**
	 * 从request中获取int参数
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	private static int getInt(HttpServletRequest request, String paramName, int defaultValue) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue) && StringUtils.isNumeric(paramValue)) {
			return Integer.parseInt(paramValue);
		} else if (StringUtils.isNotEmpty(paramValue)
				&& StringUtils.isNumeric(paramValue.substring(1))) {
			return Integer.parseInt(paramValue);
		} else {
			if (StringUtils.isNotEmpty(paramValue)) {
				try {
					return Integer.parseInt(paramValue);
				} catch (Exception ignored) {
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 从request中获取int参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static Integer getInteger(HttpServletRequest request, String paramName) {
		return getInteger(request, paramName, null);
	}

	/**
	 * 从request中获取int参数
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	private static Integer getInteger(HttpServletRequest request, String paramName, Integer defaultValue) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue) && StringUtils.isNumeric(paramValue)) {
			return Integer.parseInt(paramValue);
		} else if (StringUtils.isNotEmpty(paramValue)
				&& StringUtils.isNumeric(paramValue.substring(1))) {
			return Integer.parseInt(paramValue);
		} else {
			if (StringUtils.isNotEmpty(paramValue)) {
				try {
					return Integer.parseInt(paramValue);
				} catch (Exception ignored) {
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 获取int型字段值
	 * 
	 * @param request
	 *            HttpServletRequest request对象
	 * @param param
	 *            String 参数
	 * @param defaultNum
	 *            int 缺省值
	 * @return int 返回值
	 */
	public static int getIntParameter(HttpServletRequest request, String param, int defaultNum) {
		String temp = getParameter(request, param);
		if (StringUtils.isNumeric(temp) && StringUtils.isNumeric(temp)) {
			int num = defaultNum;
			try {
				num = Integer.parseInt(temp);
			} catch (Exception ignored) {
			}
			return num;
		} else if (StringUtils.isNumeric(temp) && StringUtils.isNumeric(temp.substring(1))) {
			int num = defaultNum;
			try {
				num = Integer.parseInt(temp);
			} catch (Exception ignored) {
			}
			return num;
		}
		return defaultNum;
	}

	/**
	 * 获取Web客户端的真实IP地址 <br/>
	 * 特别说明 ：nginx代理需要增加头信息
	 * 
	 * @param request
	 * @return
	 */
	// proxy_redirect off;
	// proxy_set_header host $host;
	// proxy_set_header x-real-ip $remote_addr;
	// proxy_set_header x-forwarded-for $proxy_add_x_forwarded_for;
	private static String getIPAddress(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-real-ip");
		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("x-forwarded-for");
		}
		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}

		if (ipAddress != null && ipAddress.length() > 15) {
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

	public static String getLayoutTheme(HttpServletRequest request) {
		String layoutTheme = "flat_home";
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equals(cookie.getName(), Constants.LAYOUT_THEME_COOKIE)) {
					if (cookie.getValue() != null) {
						layoutTheme = cookie.getValue();
					}
				}
			}
		}
		if (StringUtils.isNotEmpty(request.getParameter("layoutTheme"))) {
			layoutTheme = request.getParameter("layoutTheme");
		}
		return layoutTheme;
	}

	public static String getLocalAddress(HttpServletRequest request) {
		// reads local address
		String host = getLocalHostAddress(request, true);
		return host + request.getContextPath();
	}

	public static String getLocalHostAddress(HttpServletRequest request) {
		return getLocalHostAddress(request, false);
	}

	private static String getLocalHostAddress(HttpServletRequest request, boolean includePort) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		String port = "";
		if (includePort) {
			int p = request.getServerPort();
			port = (p == 80) ? "" : ":" + p;
		}
		return scheme + "://" + serverName + port;
	}

	public static LoginContext getLoginContext(HttpServletRequest request) {
		String actorId = getActorId(request);
		if (StringUtils.isNotEmpty(actorId)) {
			return IdentityFactory.getLoginContext(actorId);
		}
		return null;
	}

	/**
	 * 从request中获取long参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static long getLong(HttpServletRequest request, String paramName) {
		return getLong(request, paramName, 0L);
	}

	/**
	 * 从request中获取long参数
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	private static long getLong(HttpServletRequest request, String paramName, long defaultValue) {
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotEmpty(paramValue) && StringUtils.isNumeric(paramValue)) {
			return Long.parseLong(paramValue);
		} else if (StringUtils.isNotEmpty(paramValue)
				&& StringUtils.isNumeric(paramValue.substring(1))) {
			return Long.parseLong(paramValue);
		} else {
			if (StringUtils.isNotEmpty(paramValue)) {
				try {
					return Integer.parseInt(paramValue);
				} catch (Exception ignored) {
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 获取long型字段值
	 * 
	 * @param request
	 * @param param
	 * @param defaultNum
	 * @return
	 */
	public static long getLongParameter(HttpServletRequest request, String param, long defaultNum) {
		String temp = getParameter(request, param);
		if (StringUtils.isNotEmpty(temp) && StringUtils.isNumeric(temp)) {
			long num = defaultNum;
			try {
				num = Long.parseLong(temp);
			} catch (Exception ignored) {
			}
			return num;
		} else if (StringUtils.isNotEmpty(temp) && StringUtils.isNumeric(temp.substring(1))) {
			long num = defaultNum;
			try {
				num = Long.parseLong(temp);
			} catch (Exception ignored) {
			}
			return num;
		}
		return defaultNum;
	}

	/**
	 * 从request中封装一个对象
	 * 
	 * @param request
	 *            request对象
	 * @param object
	 *            封装对象的类
	 * @return 超类
	 * @throws ServletException
	 */
	public static Object getParameter(HttpServletRequest request, Object object) throws ServletException {
		Map<String, Object> dataMap = new java.util.HashMap<String, Object>();
		Enumeration<?> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = (String) enumeration.nextElement();
			String paramValue = getParameter(request, paramName);
			if (StringUtils.isNotEmpty(paramName) && StringUtils.isNotEmpty(paramValue)) {
				if (paramValue.equalsIgnoreCase("null")) {
					continue;
				}
				dataMap.put(paramName, paramValue);
				if (paramName.toLowerCase().trim().indexOf("date") > 0) {
					try {
						Date date = DateUtils.toDate(paramValue);
						dataMap.put(paramName, date);
					} catch (Exception ignored) {
					}
				}
			}
		}
		try {
			BeanUtils.populate(object, dataMap);
		} catch (Exception ex) {
			throw new ServletException(ex.getMessage());
		}
		return object;
	}

	/**
	 * 从request中获取参数
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	private static String getParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (StringUtils.isEmpty(value)) {
			String[] values = request.getParameterValues(name);
			if (values != null && values.length > 0) {
				StringBuilder buff = new StringBuilder();
				for (int i = 0; i < values.length; i++) {
					if (i < values.length - 1) {
						if (StringUtils.isNotEmpty(values[i])) {
							buff.append(values[i]).append(',');
						}
					} else {
						if (StringUtils.isNotEmpty(values[i])) {
							buff.append(values[i]);
						}
					}
				}
				if (StringUtils.isNotEmpty(buff.toString())) {
					value = buff.toString();
				}
			}
		}
		return value;
	}

	/**
	 * 获取字段值
	 * 
	 * @param request
	 *            HttpServletRequest request对象
	 * @param param
	 *            String 参数
	 * @param defaultValue
	 *            String 缺省值
	 * @return String 返回值，缺省为空
	 */
	public static String getParameter(HttpServletRequest request, String param, String defaultValue) {
		String value = request.getParameter(param);
		if (value == null || value.length() == 0) {
			if (defaultValue != null)
				value = defaultValue;
			else
				value = "";
		} else {
			value = value.trim();
		}

		return value;
	}

	/**
	 * 从request中获取参数
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getParameterMap(HttpServletRequest request) {
		Map<String, Object> dataMap = new java.util.HashMap<String, Object>();
		Enumeration<?> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = (String) enumeration.nextElement();
			String paramValue = getParameter(request, paramName);
			if (StringUtils.isNotEmpty(paramName) && StringUtils.isNotEmpty(paramValue)) {
				if (paramValue.equalsIgnoreCase("null")) {
					continue;
				}
				dataMap.put(paramName, paramValue);
				String tmp = paramName.trim().toLowerCase();
				if (tmp.indexOf("date") > 0 && !StringUtils.equals(paramValue, "asc")
						&& !StringUtils.equals(paramValue, "desc")) {
					try {
						logger.debug(paramName + " value:" + paramValue);
						Date date = DateUtils.toDate(paramValue);
						dataMap.put(tmp, date);
					} catch (java.lang.Throwable ignored) {
					}
				} else if (tmp.startsWith("x_encode_")) {
					String name = StringTools.replace(paramName, "x_encode_", "");
					dataMap.put(name, decodeString(paramValue));
				} else if (tmp.startsWith("x_collection_")) {
					String name = StringTools.replace(paramName, "x_collection_", "");
					dataMap.put(name, StringTools.split(paramValue));
				}
			}
		}
		return dataMap;
	}

	/**
	 * 从request中获取参数
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getParameterMap(HttpServletRequest request, String prefix) {
		Map<String, Object> dataMap = new java.util.HashMap<String, Object>();
		Enumeration<?> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = (String) enumeration.nextElement();
			String paramValue = getParameter(request, paramName);
			if (paramName != null && paramValue != null) {
				if (paramValue.trim().length() > 0 && !"null".equalsIgnoreCase(paramValue)) {
					if (!paramName.startsWith(prefix)) {
						continue;
					}
					paramName = paramName.substring(prefix.length());
					dataMap.put(paramName, paramValue);
				}
			}
		}
		return dataMap;
	}

	/**
	 * 从request中获取参数
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getParameterMap(MultipartHttpServletRequest request) {
		Map<String, Object> dataMap = new java.util.HashMap<String, Object>();
		dataMap.put("contextPath", request.getContextPath());
		Enumeration<?> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = (String) enumeration.nextElement();
			String paramValue = getParameter(request, paramName);
			if (StringUtils.isNotEmpty(paramName) && StringUtils.isNotEmpty(paramValue)) {
				if (paramValue.equalsIgnoreCase("null")) {
					continue;
				}
				dataMap.put(paramName, paramValue);
				String tmp = paramName.trim().toLowerCase();
				if (tmp.indexOf("date") > 0) {
					try {
						Date date = DateUtils.toDate(paramValue);
						dataMap.put(paramName, date);
					} catch (Exception ignored) {
					}
				} else if (tmp.startsWith("x_encode_")) {
					String name = StringTools.replace(paramName, "x_encode_", "");
					dataMap.put(name, decodeString(paramValue));
				}
			}
		}
		return dataMap;
	}

	/**
	 * 获取相对路径
	 * 
	 * @param request
	 * @param url
	 * @return
	 */
	public static String getRelativeUrl(HttpServletRequest request, String url) {
		if (!url.startsWith(getLocalHostAddress(request, true) + "/")) {
			return url;
		}
		url = url.replace((getLocalHostAddress(request, true) + "/"), "");
		String actualServlet = request.getContextPath() + request.getServletPath();
		if (actualServlet.startsWith("/")) {
			actualServlet = actualServlet.substring(1);
		}
		int i = actualServlet.indexOf("/");
		StringBuilder urlBuilder = new StringBuilder(url);
		while (i != -1) {
			if (urlBuilder.toString().startsWith(actualServlet.substring(0, i))) {
				urlBuilder = new StringBuilder(urlBuilder.toString().replace(actualServlet.substring(0, i + 1), ""));
			} else {
				urlBuilder.insert(0, "../");
			}
			actualServlet = actualServlet.substring(i + 1);
			i = actualServlet.indexOf("/");
		}
		url = urlBuilder.toString();
		return url;
	}

	public static String getRequestParameters(HttpServletRequest request) {
		Map<String, Object> m = getParameterMap(request);
		m.remove("x");
		m.remove("y");
		m.remove("redirectUrl");
		return createQueryStringFromMap(m, "&").toString();
	}

	public static String getServiceUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String serviceUrl = scheme + "://" + request.getServerName();
		if (request.getServerPort() != 80) {
			serviceUrl += ":" + request.getServerPort();
		}
		if (!"/".equals(request.getContextPath())) {
			serviceUrl += request.getContextPath();
		}
		return serviceUrl;
	}

	public static LoginContext getSessionLoginContext(HttpServletRequest request) {
		String actorId = null;
		String ip = getIPAddress(request);
		ip = DigestUtils.md5Hex(ip + SystemConfig.getIntToken());
		HttpSession session = request.getSession(false);
		if (session != null) {
			String value = (String) session.getAttribute(Constants.LOGIN_INFO);
			Map<String, String> cookieMap = decodeValues(ip, value);
			if (StringUtils.equals(cookieMap.get(Constants.LOGIN_IP), ip)) {
				actorId = cookieMap.get(Constants.LOGIN_ACTORID);
			}
		}

		if (StringUtils.isNotEmpty(actorId)) {
			return IdentityFactory.getLoginContext(actorId);
		}

		return null;
	}

	/**
	 * 从request中获取字符串参数
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	public static String getString(HttpServletRequest request, String paramName) {
		String paramValue = getParameter(request, paramName);
		if (StringUtils.isNotEmpty(paramValue)) {
			return paramValue;
		}
		return null;
	}

	/**
	 * 从request中获取字符串参数
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	public static String getString(HttpServletRequest request, String paramName, String defaultValue) {
		String paramValue = getParameter(request, paramName);
		if (StringUtils.isNotEmpty(paramValue)) {
			return paramValue;
		}
		return defaultValue;
	}

	/**
	 * 获取参数值
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getStringValue(HttpServletRequest request, String name) {
		if (request.getAttribute(name) != null) {
			return (String) request.getAttribute(name);
		}
		return request.getParameter(name);
	}

	/**
	 * 获取系统名称 （即要访问的数据源定义名称）
	 * 
	 * @param request
	 * @return
	 */
	public static String getSystemName(HttpServletRequest request) {
		String systemName = request.getParameter("systemName");
		if (StringUtils.isEmpty(systemName)) {
			systemName = getCurrentSystem(request);
		}
		if (StringUtils.isEmpty(systemName)) {
			systemName = com.glaf.core.config.Environment.DEFAULT_SYSTEM_NAME;
		}
		return systemName;
	}

	public static String getTheme(HttpServletRequest request) {
		String theme = "default";
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equals(cookie.getName(), Constants.THEME_COOKIE)) {
					if (cookie.getValue() != null) {
						theme = cookie.getValue();
					}
				}
			}
		}
		if (StringUtils.isNotEmpty(request.getParameter("theme"))) {
			theme = request.getParameter("theme");
		}
		return theme;
	}

	public static boolean isMobile(HttpServletRequest request) {
		boolean isMobile = false;
		if (StringUtils.equals(request.getParameter("mobile"), "true")) {
			isMobile = true;
		}
		String userAgent = request.getHeader("user-agent");
		if (StringUtils.containsIgnoreCase(userAgent, "Android")) {
			isMobile = true;
		}
		if (StringUtils.containsIgnoreCase(userAgent, "iPhone")) {
			isMobile = true;
		}
		if (StringUtils.containsIgnoreCase(userAgent, "Windows Phone")) {
			isMobile = true;
		}
		return isMobile;
	}

	public static void removeLoginUser(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equals(cookie.getName(), Constants.COOKIE_NAME)) {
					cookie.setMaxAge(0);
					cookie.setPath("/");
					cookie.setValue(UUID32.getUUID());
					response.addCookie(cookie);
					logger.debug("remove user from cookie");
				}
			}
		}

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(Constants.LOGIN_INFO);
			session.invalidate();
		}
	}

	public static void setCurrentUser(HttpServletRequest request, HttpServletResponse response, String systemName,
			String actorId) {
		String ip = getIPAddress(request);
		ip = DigestUtils.md5Hex(ip + SystemConfig.getIntToken());
		String value = encodeValues(ip, systemName, actorId);
		// logger.debug("value->" + value);
		try {
			response.addHeader(Constants.AUTH_NAME, value);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void setLoginUser(HttpServletRequest request, HttpServletResponse response, String systemName,
			String actorId) {
		String ip = getIPAddress(request);
		ip = DigestUtils.md5Hex(ip + SystemConfig.getIntToken());
		String value = encodeValues(ip, systemName, actorId);
		logger.debug("value->" + value);
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.setAttribute(Constants.LOGIN_INFO, value);
		}
		try {
			Cookie cookie = new Cookie(Constants.COOKIE_NAME, value);
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			response.addCookie(cookie);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 设置参数
	 * 
	 * @param request
	 */
	public static void setRequestParameterToAttribute(HttpServletRequest request) {
		request.setAttribute("contextPath", request.getContextPath());
		Enumeration<?> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = (String) enumeration.nextElement();
			String paramValue = getParameter(request, paramName);
			if (StringUtils.isNotEmpty(paramName) && StringUtils.isNotEmpty(paramValue)) {
				if (request.getAttribute(paramName) != null) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("method", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("action", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("actionType", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("reset", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("submit", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("button", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("cancel", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("parent", paramName)) {
					continue;
				} else if (StringUtils.equalsIgnoreCase("hasPermission", paramName)) {
					continue;
				}
				request.setAttribute(paramName, paramValue);
			}
		}
	}

	public static void setTheme(HttpServletRequest request, HttpServletResponse response) {
		String theme = request.getParameter("theme");
		if (StringUtils.isNotEmpty(theme)) {
			Cookie cookie = new Cookie(Constants.THEME_COOKIE, theme);
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			response.addCookie(cookie);

			Cookie cookie2 = new Cookie("data-theme", "theme-" + theme);
			cookie2.setPath("/");
			cookie2.setMaxAge(-1);
			response.addCookie(cookie2);
		}

		String homeTheme = request.getParameter("homeTheme");
		if (StringUtils.isNotEmpty(homeTheme)) {
			Cookie cookie5 = new Cookie(Constants.HOME_THEME_COOKIE, homeTheme);
			cookie5.setPath("/");
			cookie5.setMaxAge(-1);
			response.addCookie(cookie5);
		}
	}

	public static void setTheme(HttpServletRequest request, HttpServletResponse response, String theme) {
		if (StringUtils.isNotEmpty(theme)) {
			Cookie cookie = new Cookie(Constants.THEME_COOKIE, theme);
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			response.addCookie(cookie);

			Cookie cookie2 = new Cookie("data-theme", "theme-" + theme);
			cookie2.setPath("/");
			cookie2.setMaxAge(-1);
			response.addCookie(cookie2);
		}
	}

	public static void setTheme(HttpServletRequest request, HttpServletResponse response, String layout, String theme,
			String homeTheme) {
		if (StringUtils.isNotEmpty(theme)) {
			Cookie cookie = new Cookie(Constants.THEME_COOKIE, theme);
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			response.addCookie(cookie);

			Cookie cookie2 = new Cookie("data-theme", "theme-" + theme);
			cookie2.setPath("/");
			cookie2.setMaxAge(-1);
			response.addCookie(cookie2);
		}

		if (StringUtils.isNotEmpty(homeTheme)) {
			Cookie cookie = new Cookie(Constants.HOME_THEME_COOKIE, homeTheme);
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			response.addCookie(cookie);
		}
		if (StringUtils.isNotEmpty(layout)) {
			Cookie cookie = new Cookie(Constants.LAYOUT_THEME_COOKIE, layout);
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			response.addCookie(cookie);
		}
	}

}