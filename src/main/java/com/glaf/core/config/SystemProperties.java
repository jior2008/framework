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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.glaf.core.util.Constants;

public class SystemProperties {

	protected static AtomicBoolean loading = new AtomicBoolean(false);

	protected static final String DEPLOYMENT_SYSTEM_NAME = "deploymentSystemName";

	private static volatile String ROOT_CONF_PATH = null;

	private static volatile String ROOT_APP_PATH = null;

	/**
	 * 获取系统名称
	 * 
	 * @return
	 */
	public static String getAppName() {
		if (System.getProperty(Constants.APP_NAME) != null) {
			return System.getProperty(Constants.APP_NAME);
		}
		return "glaf";
	}

	public static String getAppPath() {
		if (ROOT_APP_PATH == null) {
			reload();
		}
		return ROOT_APP_PATH;
	}

	/**
	 * 返回web应用的WEB-INF目录的全路径
	 * 
	 * @return
	 */
	public static String getConfigRootPath() {
		if (ROOT_CONF_PATH == null) {
			reload();
		}
		if (ROOT_CONF_PATH == null) {
			ROOT_CONF_PATH = System.getProperty("app.path");
		}
		return ROOT_CONF_PATH;
	}

	/**
	 * 获取系统部署名称
	 * 
	 * @return
	 */
	public static String getDeploymentSystemName() {
		if (System.getProperty(DEPLOYMENT_SYSTEM_NAME) != null) {
			return System.getProperty(DEPLOYMENT_SYSTEM_NAME);
		}
		return null;
	}

	/**
	 * 获取主数据库数据源配置文件
	 * 
	 * @return
	 */
	public static String getMasterDataSourceConfigFile() {
		String deploymentSystemName = getDeploymentSystemName();
		if (deploymentSystemName != null && deploymentSystemName.length() > 0) {
			return Constants.DEPLOYMENT_JDBC_PATH + deploymentSystemName + "/jdbc.properties";
		}
		return Constants.DEFAULT_MASTER_JDBC_CONFIG;
	}

	public static String getRegionName(String region) {
		if (getDeploymentSystemName() != null) {
			return getDeploymentSystemName() + "_" + region;
		}
		return region;
	}

	public static void reload() {
		if (!loading.get()) {
			try {
				loading.set(true);
				Resource resource = new ClassPathResource(Constants.SYSTEM_CONFIG);
				ROOT_CONF_PATH = resource.getFile().getParentFile().getAbsolutePath();
				ROOT_APP_PATH = resource.getFile().getParentFile().getAbsolutePath();
				System.out.println("load system config:" + resource.getFile().getAbsolutePath());
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} finally {
				loading.set(false);
			}
		}
	}

	private SystemProperties() {

	}

}