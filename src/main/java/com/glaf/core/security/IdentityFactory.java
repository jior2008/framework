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

package com.glaf.core.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.cache.CacheFactory;
import com.glaf.core.context.ContextFactory;
import com.glaf.core.identity.Role;
import com.glaf.core.identity.User;
import com.glaf.core.identity.util.UserJsonFactory;
import com.glaf.core.service.EntityService;
import com.glaf.core.util.Constants;
import com.glaf.core.util.hash.JenkinsHash;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IdentityFactory {
	protected final static Log logger = LogFactory.getLog(IdentityFactory.class);

	private static EntityService entityService;

	private static EntityService getEntityService() {
		if (entityService == null) {
			entityService = ContextFactory.getBean("entityService");
		}
		return entityService;
	}

	/**
	 * 获取登录用户信息
	 * 
	 * @param userId
	 * @return
	 */
	public static LoginContext getLoginContext(String userId) {
		String cacheKey = Constants.CACHE_LOGIN_CONTEXT_KEY + userId;
		String text = CacheFactory.getString(Constants.CACHE_LOGIN_CONTEXT_REGION, cacheKey);
		if (StringUtils.isNotEmpty(text)) {
			try {
				JSONObject jsonObject = JSON.parseObject(text);
				return LoginContextUtils.jsonToObject(jsonObject);
			} catch (Exception ignored) {
			}
		}

		User user = getUser(userId);
		if (user != null) {
			LoginContext loginContext = new LoginContext(user);
			CacheFactory.put(Constants.CACHE_LOGIN_CONTEXT_REGION, cacheKey,
					loginContext.toJsonObject().toJSONString());
			return loginContext;
		}

		return null;
	}

	public static List<Role> getRoles() {
		List<Role> roles = new ArrayList<Role>();
		List<Object> list = getEntityService().getList("getRoles", null);
		if (list != null && !list.isEmpty()) {
			for (Object object : list) {
				if (object instanceof Role) {
					Role role = (Role) object;
					roles.add(role);
				}
			}
		}
		return roles;
	}

	/**
	 * 通过租户编号获取租户Hash码
	 * 
	 * @param tenantId
	 * @return
	 */
	public static int getTenantHash(String tenantId) {
		if (tenantId != null) {
			return Math.abs(JenkinsHash.getInstance().hash(tenantId.getBytes()))
					% com.glaf.core.util.Constants.TABLE_PARTITION;
		}
		return 0;
	}

	/**
	 * 根据用户名获取用户对象
	 * 
	 * @param userId
	 * @return
	 */
	public static User getUser(String userId) {
		String cacheKey = Constants.CACHE_USER_KEY + userId;
		String text = CacheFactory.getString(Constants.CACHE_USER_REGION, cacheKey);
		if (text != null) {
			try {
				JSONObject jsonObject = JSON.parseObject(text);
				return UserJsonFactory.jsonToObject(jsonObject);
			} catch (Exception ignored) {
			}
		}
		User user = (User) getEntityService().getById("getUserById", userId);
		if (user != null) {
			CacheFactory.put(Constants.CACHE_USER_REGION, cacheKey, user.toJsonObject().toJSONString());
		}
		return user;
	}

	public static List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		List<Object> list = getEntityService().getList("getUsers", null);
		if (list != null && !list.isEmpty()) {
			for (Object object : list) {
				if (object instanceof User) {
					User u = (User) object;
					users.add(u);
				}
			}
		}
		return users;
	}

	public static void setEntityService(EntityService entityService) {
		IdentityFactory.entityService = entityService;
	}

	private IdentityFactory() {

	}

}
