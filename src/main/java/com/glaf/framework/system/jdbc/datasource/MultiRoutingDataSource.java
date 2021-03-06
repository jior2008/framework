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
import com.glaf.framework.system.config.DBConfiguration;
import com.glaf.framework.system.jdbc.connection.ConnectionProvider;
import com.glaf.framework.system.jdbc.connection.ConnectionProviderFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiRoutingDataSource extends AbstractRoutingDataSource
		implements ApplicationListener<ContextRefreshedEvent> {
	private final static Log logger = LogFactory.getLog(MultiRoutingDataSource.class);

	private static final ConcurrentMap<Object, Object> targetDataSources = new ConcurrentHashMap<Object, Object>();

	private static final AtomicBoolean loading = new AtomicBoolean(false);

	private static final AtomicBoolean reload = new AtomicBoolean(false);

	private static volatile Object defaultTargetDataSource;

	public static void addDataSource(String name, Properties props) {
		if (!targetDataSources.containsKey(name)) {
			ConnectionProvider provider = ConnectionProviderFactory.createProvider(name, props);
			if (provider != null && provider.getDataSource() != null) {
				targetDataSources.put(name, provider.getDataSource());
				reload.set(true);
			}
		}
	}

	private static void reloadDS() {
		if (!loading.get()) {
			try {
				loading.set(true);
				logger.info("--------------MultiRoutingDataSource reloadDS()------------");
				Map<Object, Object> dataSourceMap = new java.util.HashMap<Object, Object>();
				Map<String, Properties> dataSourceProperties = DBConfiguration.getDataSourceProperties();
				Set<Entry<String, Properties>> entrySet = dataSourceProperties.entrySet();
				for (Entry<String, Properties> entry : entrySet) {
					String name = entry.getKey();
					Properties props = entry.getValue();
					if (props != null && StringUtils.isNotEmpty(name)) {
						try {
							ConnectionProvider provider = ConnectionProviderFactory.createProvider(name);
							if (provider != null) {
								dataSourceMap.put(name, provider.getDataSource());
								if (StringUtils.equals(name, Environment.DEFAULT_SYSTEM_NAME)) {
									defaultTargetDataSource = provider.getDataSource();
								}
							}
						} catch (Exception ex) {

							logger.error(ex);
						}
					}
				}

				if (defaultTargetDataSource == null) {
					Properties props = DBConfiguration.getProperties(Environment.DEFAULT_SYSTEM_NAME);
					if (props != null) {
						ConnectionProvider provider = ConnectionProviderFactory
								.createProvider(Environment.DEFAULT_SYSTEM_NAME, props);
						dataSourceMap.put(Environment.DEFAULT_SYSTEM_NAME, provider.getDataSource());
					}
				}

				targetDataSources.putAll(dataSourceMap);

				logger.info("##datasources:" + targetDataSources.keySet());

			} catch (Exception ex) {
				logger.error(ex);

				throw new RuntimeException(ex);
			} finally {
				loading.set(false);
			}
		}
	}

	private MultiRoutingDataSource() {

	}

	@Override
	public void afterPropertiesSet() {
		reloadDS();
		setDefaultTargetDataSource(defaultTargetDataSource);
		setTargetDataSources(targetDataSources);
		super.afterPropertiesSet();
	}

	@Override
	protected synchronized Object determineCurrentLookupKey() {
		if (reload.get()) {
			setTargetDataSources(targetDataSources);
			super.afterPropertiesSet();
			reload.set(false);
			logger.info("#reload datasources:" + targetDataSources.keySet());
		}
		logger.debug("currentSystemName:" + Environment.getCurrentSystemName());
		return Environment.getCurrentSystemName();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		logger.info("-----------------DataSource Launcher----------------------");
		reloadDS();
		setDefaultTargetDataSource(defaultTargetDataSource);
		setTargetDataSources(targetDataSources);
	}

}