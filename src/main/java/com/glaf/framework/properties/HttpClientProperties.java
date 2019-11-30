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

package com.glaf.framework.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

/**
 * HttpClient连接池配置
 *
 */
@Component
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "httpclient")
public class HttpClientProperties {
	/**
	 * 建立连接的超时时间
	 */
	private int connectTimeout = 20000;
	/**
	 * 连接不够用的等待时间
	 */
	private int requestTimeout = 20000;
	/**
	 * 每次请求等待返回的超时时间
	 */
	private int socketTimeout = 30000;
	/**
	 * 每个主机最大连接数
	 */
	private int defaultMaxPerRoute = 100;
	/**
	 * 最大连接数
	 */
	private int maxTotalConnections = 300;
	/**
	 * 默认连接保持活跃的时间
	 */
	private int defaultKeepAliveTimeMillis = 20000;
	/**
	 * 空闲连接生的存时间
	 */
	private int closeIdleConnectionWaitTimeSecs = 30;

	public int getCloseIdleConnectionWaitTimeSecs() {
		return closeIdleConnectionWaitTimeSecs;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getDefaultKeepAliveTimeMillis() {
		return defaultKeepAliveTimeMillis;
	}

	public int getDefaultMaxPerRoute() {
		return defaultMaxPerRoute;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setCloseIdleConnectionWaitTimeSecs(int closeIdleConnectionWaitTimeSecs) {
		this.closeIdleConnectionWaitTimeSecs = closeIdleConnectionWaitTimeSecs;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setDefaultKeepAliveTimeMillis(int defaultKeepAliveTimeMillis) {
		this.defaultKeepAliveTimeMillis = defaultKeepAliveTimeMillis;
	}

	public void setDefaultMaxPerRoute(int defaultMaxPerRoute) {
		this.defaultMaxPerRoute = defaultMaxPerRoute;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
}
