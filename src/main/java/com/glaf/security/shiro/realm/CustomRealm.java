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
package com.glaf.security.shiro.realm;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.glaf.core.security.IdentityFactory;
import com.glaf.core.security.LoginContext;
import com.glaf.security.shiro.model.JWTToken;
import com.glaf.security.shiro.util.JWTUtil;

@Component
public class CustomRealm extends AuthorizingRealm {
	private Logger logger = LoggerFactory.getLogger(CustomRealm.class);

	/**
	 * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		logger.debug("————身份认证方法————");
		String token = (String) authenticationToken.getCredentials();
		// 解密获得userId，用于和数据库进行对比
		String userId = JWTUtil.getUserId(token);
		if (userId == null || !JWTUtil.verify(token, userId)) {
			throw new AuthenticationException("token认证失败！");
		}
		return new SimpleAuthenticationInfo(token, token, "MyRealm");
	}

	/**
	 * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		logger.debug("————权限认证————");
		String userId = JWTUtil.getUserId(principals.toString());
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

		Set<String> roleSet = new HashSet<>();
		Set<String> permissionSet = new HashSet<>();
		// 需要将 role, permission 封装到 Set 作为 info.setRoles(), info.setStringPermissions()
		// 的参数
		LoginContext loginContext = IdentityFactory.getLoginContext(userId);
		if (loginContext.getRoles() != null) {
			roleSet.addAll(loginContext.getRoles());
			permissionSet.addAll(loginContext.getRoles());
		}

		if (loginContext.getPermissions() != null) {
			permissionSet.addAll(loginContext.getPermissions());
		}
		// 设置该用户拥有的角色和权限
		info.setRoles(roleSet);
		info.setStringPermissions(permissionSet);
		return info;
	}

	/**
	 * 必须重写此方法，不然会报错
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof JWTToken;
	}
}
