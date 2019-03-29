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

package com.glaf.framework.system.service.impl;

import com.glaf.core.util.UUID32;
import com.glaf.framework.system.config.SystemConfig;
import com.glaf.framework.system.domain.SystemProperty;
import com.glaf.framework.system.mapper.SystemPropertyMapper;
import com.glaf.framework.system.query.SystemPropertyQuery;
import com.glaf.framework.system.service.ISystemPropertyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service("systemPropertyService")
@Transactional(readOnly = true)
public class SystemPropertyServiceImpl implements ISystemPropertyService {
	protected final static Log logger = LogFactory.getLog(SystemPropertyServiceImpl.class);


	private SystemPropertyMapper systemPropertyMapper;

	public SystemPropertyServiceImpl() {

	}

	@Cacheable(cacheNames = "propertyCount")
	public int count(SystemPropertyQuery query) {
		return systemPropertyMapper.getSystemPropertyCount(query);
	}

	@CacheEvict(cacheNames = { "property", "propertyCount", "properties" }, allEntries = true)
	@Transactional
	public void deleteById(String id) {
		systemPropertyMapper.deleteSystemPropertyById(id);
	}

	@Cacheable(cacheNames = "properties")
	public List<SystemProperty> getAllSystemProperties() {
		SystemPropertyQuery query = new SystemPropertyQuery();
		List<SystemProperty> list = this.list(query);
		List<SystemProperty> rows = new ArrayList<SystemProperty>();
		if (list != null && !list.isEmpty()) {
			for (SystemProperty p : list) {
				if (!StringUtils.equals("TOKEN", p.getId())) {
					rows.add(p);
				}
			}
		}

		return rows;
	}

	@Cacheable(cacheNames = "properties")
	public Map<String, SystemProperty> getProperyMap() {
		List<SystemProperty> list = this.getAllSystemProperties();
		Map<String, SystemProperty> dataMap = new java.util.HashMap<String, SystemProperty>();
		for (SystemProperty p : list) {
			dataMap.put(p.getName(), p);
		}
		return dataMap;
	}

	@Cacheable(cacheNames = "properties")
	public List<SystemProperty> getSystemProperties(String category) {
		SystemPropertyQuery query = new SystemPropertyQuery();
		query.category(category);
		List<SystemProperty> list = this.list(query);
		List<SystemProperty> rows = new ArrayList<SystemProperty>();
		if (list != null && !list.isEmpty()) {
			for (SystemProperty p : list) {
				if (!StringUtils.equals("TOKEN", p.getId())) {
					rows.add(p);
				}
			}
		}

		return rows;
	}

	@Cacheable(cacheNames = "propertiy")
	public SystemProperty getSystemProperty(String category, String name) {
		SystemPropertyQuery query = new SystemPropertyQuery();
		query.category(category);
		query.name(name);
		List<SystemProperty> list = this.list(query);
		if (list != null && !list.isEmpty()) {
            return list.get(0);
		}
		return null;
	}

	@Cacheable(cacheNames = "propertiy")
	public SystemProperty getSystemPropertyById(String id) {
        return systemPropertyMapper.getSystemPropertyById(id);
	}

	@Cacheable(cacheNames = "properties")
	public List<SystemProperty> list(SystemPropertyQuery query) {
		List<SystemProperty> list = systemPropertyMapper.getSystemProperties(query);
		List<SystemProperty> rows = new ArrayList<SystemProperty>();
		if (list != null && !list.isEmpty()) {
			for (SystemProperty p : list) {
				if (!StringUtils.equals("TOKEN", p.getId())) {
					rows.add(p);
				}
			}
		}
		return rows;
	}

	@CacheEvict(cacheNames = { "property", "propertyCount", "properties" }, allEntries = true)
	@Transactional
	public void save(SystemProperty property) {
		if (StringUtils.isNotEmpty(property.getId())) {
			SystemProperty bean = this.getSystemPropertyById(property.getId());
			if (bean != null) {
				bean.setDescription(property.getDescription());
				bean.setValue(property.getValue());
				bean.setInitValue(property.getInitValue());
				bean.setInputType(property.getInputType());
				bean.setLocked(property.getLocked());
				bean.setTitle(property.getTitle());
				bean.setType(property.getType());
				systemPropertyMapper.updateSystemProperty(bean);
				SystemConfig.setProperty(bean);
			} else {
				systemPropertyMapper.insertSystemProperty(property);
				SystemConfig.setProperty(property);
			}
		} else {
			SystemPropertyQuery query = new SystemPropertyQuery();
			query.category(property.getCategory());
			query.name(property.getName());
			List<SystemProperty> list = this.list(query);
			if (list != null && !list.isEmpty()) {
				SystemProperty bean = list.get(0);
				bean.setDescription(property.getDescription());
				bean.setValue(property.getValue());
				bean.setInitValue(property.getInitValue());
				bean.setInputType(property.getInputType());
				bean.setLocked(property.getLocked());
				bean.setTitle(property.getTitle());
				bean.setType(property.getType());
				systemPropertyMapper.updateSystemProperty(bean);
				SystemConfig.setProperty(bean);
			} else {
				if (property.getId() == null) {
					property.setId(UUID32.getUUID());
				}
				systemPropertyMapper.insertSystemProperty(property);
				SystemConfig.setProperty(property);
			}
		}
	}

	@CacheEvict(cacheNames = { "property", "propertyCount", "properties" }, allEntries = true)
	@Transactional
	public void saveAll(List<SystemProperty> props) {
		Map<String, SystemProperty> propertyMap = this.getProperyMap();
		if (props != null && props.size() > 0) {
			//Map<String, String> dataMap = new TreeMap<String, String>();
			for (SystemProperty p : props) {
				//dataMap.put(p.getName(), p.getValue());
				if (propertyMap.get(p.getName()) != null) {
					SystemProperty model = propertyMap.get(p.getName());
					model.setDescription(p.getDescription());
					model.setTitle(p.getTitle());
					model.setValue(p.getValue());
					systemPropertyMapper.updateSystemProperty(model);
				} else {
					p.setId(UUID32.getUUID());
					systemPropertyMapper.insertSystemProperty(p);
				}
			}
			SystemConfig.reload();
		}
	}


	@javax.annotation.Resource
	public void setSystemPropertyMapper(SystemPropertyMapper systemPropertyMapper) {
		this.systemPropertyMapper = systemPropertyMapper;
	}

}