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

package com.glaf.framework.config;

import com.glaf.framework.system.factory.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(DruidConfig.class)
public class MultiDBConfig {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public MultiDBConfig() {
		reload();
	}

	private void reload() {
		try {
			DatabaseFactory.getInstance().reload();
			logger.info("多数据库配置装载完成。");
		} catch (java.lang.Throwable ex) {
			logger.error("multi datasource load error", ex);
			ex.printStackTrace();
		}
	}

}
