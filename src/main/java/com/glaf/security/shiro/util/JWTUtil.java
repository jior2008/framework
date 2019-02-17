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
package com.glaf.security.shiro.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.glaf.framework.system.config.SystemConfig;

import java.util.Date;

public class JWTUtil {
	// 过期时间 24 小时
	private static final long EXPIRE_TIME = 60 * 24 * 60 * 1000;
	// 密钥
	private static final String SECRET = SystemConfig.getToken();

	/**
	 * 生成 token
	 *
	 * @param userId 用户名
	 * @return 加密的token
	 */
	public static String createToken(String userId) {
		try {
			Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
			Algorithm algorithm = Algorithm.HMAC256(SECRET);
			// 附带username信息
			return JWT.create().withClaim("userId", userId)
					// 到期时间
					.withExpiresAt(date)
					// 创建一个新的JWT，并使用给定的算法进行标记
					.sign(algorithm);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获得token中的信息，无需secret解密也能获得
	 *
	 * @return token中包含的用户名
	 */
	public static String getUserId(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getClaim("userId").asString();
		} catch (JWTDecodeException e) {
			return null;
		}
	}

	/**
	 * 校验 token 是否正确
	 *
	 * @param token  密钥
	 * @param userId 用户名
	 * @return 是否正确
	 */
	public static boolean verify(String token, String userId) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(SECRET);
			// 在token中附带了userId信息
			JWTVerifier verifier = JWT.require(algorithm).withClaim("userId", userId).build();
			// 验证 token
			verifier.verify(token);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}
}
