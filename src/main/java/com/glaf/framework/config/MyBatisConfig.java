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

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@AutoConfigureAfter(DruidConfig.class)
public class MyBatisConfig {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	@ConditionalOnMissingBean // 当容器里没有指定的Bean的情况下创建该对象
	public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		// 设置数据源
		sqlSessionFactoryBean.setDataSource(dataSource);
		// 设置mybatis的主配置文件
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource mybatisConfigXml = resolver.getResource("classpath:mybatis/mybatis-config.xml");
		sqlSessionFactoryBean.setConfigLocation(mybatisConfigXml);
		List<Resource> resouresList = new ArrayList<Resource>();
		// 扫描mapper文件
		Resource[] resoures = null;
		Resource[] resoures2 = null;
		try {
			resoures = resolver.getResources("classpath:mybatis/mapper/*.xml");
			logger.info("mybatis/mapper resoures size:" + resoures.length);
			for (Resource resoure : resoures) {
				logger.info("Load XML Mapper:" + resoure.getFilename());
				resouresList.add(resoure);
			}

			resoures2 = resolver.getResources("classpath:com/glaf/**/mapper/*.xml");
			logger.info("com/glaf resoures size:" + resoures2.length);
			for (Resource resoure : resoures2) {
				logger.info("Load XML Mapper:" + resoure.getFilename());
				resouresList.add(resoure);
			}

		} catch (IOException ex) {
			// ex.printStackTrace();
			logger.error(ex.getMessage());
		}
		// Resource[] resoures=new Resource[]{mybatisMapperXml};
		resoures = new Resource[resouresList.size()];
		int index = 0;
		for (Resource resoure : resouresList) {
			resoures[index++] = resoure;
		}
		sqlSessionFactoryBean.setMapperLocations(resoures);
		logger.info("MyBatisConfig装配完成.");
		return sqlSessionFactoryBean;
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionTemplate sqlTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
