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

package com.glaf.framework.system.jdbc.datasource;

import com.glaf.core.config.Environment;
import com.glaf.core.config.SystemProperties;
import com.glaf.core.util.Constants;
import com.glaf.core.util.PropertiesUtils;
import com.glaf.framework.system.jdbc.connection.ConnectionProvider;
import com.glaf.framework.system.jdbc.connection.ConnectionProviderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class DynamicDataSource extends AbstractRoutingDataSource {
	private final static Log logger = LogFactory
			.getLog(DynamicDataSource.class);

	private static final ConcurrentMap<Object, Object> targetDataSources = new ConcurrentHashMap<Object, Object>();

	private static Object defaultTargetDataSource;

	private DynamicDataSource() {

	}

	@Override
	public void afterPropertiesSet() {
		try {
			String path;
			String deploymentSystemName = SystemProperties
					.getDeploymentSystemName();
			if (deploymentSystemName != null
					&& deploymentSystemName.length() > 0) {
				path = SystemProperties.getConfigRootPath()
						+ Constants.DEPLOYMENT_JDBC_PATH + deploymentSystemName
						+ "/jdbc/";
			} else {
				path = SystemProperties.getConfigRootPath() + "/conf/jdbc/";
			}
			String filename = path + Environment.getCurrentSystemName()
					+ ".jdbc.properties";
			Properties props = PropertiesUtils.loadFilePathResource(filename);
			if (props != null) {
				ConnectionProvider provider = ConnectionProviderFactory
						.createProvider(Environment.getCurrentSystemName(),
								props);
				targetDataSources.put(Environment.getCurrentSystemName(),
						provider.getDataSource());
				defaultTargetDataSource = provider.getDataSource();
			}
		} catch (Exception ex) {
			
			logger.error(ex);
		}
		setDefaultTargetDataSource(defaultTargetDataSource);
		setTargetDataSources(targetDataSources);
		super.afterPropertiesSet();
	}

	@Override
	protected synchronized Object determineCurrentLookupKey() {
		return Environment.getCurrentSystemName();
	}

}