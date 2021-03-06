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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.cache.CacheFactory;
import com.glaf.core.domain.Dictory;
import com.glaf.core.domain.SysTree;
import com.glaf.core.id.IdGenerator;
import com.glaf.core.query.DictoryQuery;
import com.glaf.core.service.DictoryService;
import com.glaf.core.service.SysTreeService;
import com.glaf.core.util.PageResult;
import com.glaf.framework.system.config.SystemConfig;
import com.glaf.framework.system.factory.DictoryJsonFactory;
import com.glaf.framework.system.mapper.DictoryMapper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("dictoryService")
@Transactional(readOnly = true)
public class DictoryServiceImpl implements DictoryService {
	protected final static Log logger = LogFactory.getLog(DictoryServiceImpl.class);

	private IdGenerator idGenerator;

	private DictoryMapper dictoryMapper;

	private SqlSessionTemplate sqlSessionTemplate;

	private SysTreeService sysTreeService;

	public DictoryServiceImpl() {

	}

	private int count(DictoryQuery query) {
		return dictoryMapper.getDictoryCount(query);
	}

	@Transactional
	public boolean create(Dictory bean) {
		this.save(bean);
		if (SystemConfig.getBoolean("use_query_cache")) {
			CacheFactory.clear("dictory");
		}
		return true;
	}

	@Transactional
	public boolean delete(Dictory bean) {
		this.deleteById(bean.getId());
		if (SystemConfig.getBoolean("use_query_cache")) {
			CacheFactory.clear("dictory");
		}
		return true;
	}

	@Transactional
	public boolean delete(long id) {
		this.deleteById(id);
		if (SystemConfig.getBoolean("use_query_cache")) {
			CacheFactory.clear("dictory");
		}
		return true;
	}

	@Transactional
	public boolean deleteAll(long[] ids) {
		if (ids != null && ids.length > 0) {
			for (long id : ids) {
				this.deleteById(id);
			}
		}
		if (SystemConfig.getBoolean("use_query_cache")) {
			CacheFactory.clear("dictory");
		}
		return true;
	}

	@Transactional
	public void deleteById(Long id) {
		if (id != null) {
			dictoryMapper.deleteDictoryById(id);
			if (SystemConfig.getBoolean("use_query_cache")) {
				CacheFactory.clear("dictory");
			}
		}
	}

	public Dictory find(long id) {
		return this.getDictory(id);
	}

	public List<Dictory> getAvailableDictoryList(long nodeId) {
		String cacheKey = "sys_dicts_" + nodeId;
		if (SystemConfig.getBoolean("use_query_cache")) {
			String text = CacheFactory.getString("dictory", cacheKey);
			if (StringUtils.isNotEmpty(text)) {
				try {
					JSONArray array = JSON.parseArray(text);
					return DictoryJsonFactory.arrayToList(array);
				} catch (Exception ignored) {
				}
			}
		}

		DictoryQuery query = new DictoryQuery();
		query.nodeId(nodeId);
		query.setOrderBy(" E.SORTNO asc");
		List<Dictory> list = this.list(query);
		if (list != null && !list.isEmpty()) {
			if (SystemConfig.getBoolean("use_query_cache")) {
				JSONArray array = DictoryJsonFactory.listToArray(list);
				CacheFactory.put("dictory", cacheKey, array.toJSONString());
			}
		}
		return list;
	}

	public String getCodeById(long id) {
		Dictory dic = find(id);
		return dic.getCode();
	}

	public List<Dictory> getDictories(DictoryQuery query) {
		return dictoryMapper.getDictories(query);
	}

	public List<Dictory> getDictories(String codeLike) {
		DictoryQuery query = new DictoryQuery();
		query.setCodeLike(codeLike);
		return dictoryMapper.getDictories(query);
	}

	private Dictory getDictory(Long id) {
		if (id == null) {
			return null;
		}
		String cacheKey = "sys_dict_" + id;
		if (SystemConfig.getBoolean("use_query_cache")) {
			String text = CacheFactory.getString("dictory", cacheKey);
			if (StringUtils.isNotEmpty(text)) {
				try {
					JSONObject json = JSON.parseObject(text);
					return DictoryJsonFactory.jsonToObject(json);
				} catch (Exception ignored) {
				}
			}
		}

		Dictory dictory = dictoryMapper.getDictoryById(id);

		if (dictory != null && SystemConfig.getBoolean("use_query_cache")) {
			JSONObject json = dictory.toJsonObject();
			CacheFactory.put("dictory", cacheKey, json.toJSONString());
		}
		return dictory;
	}

	public int getDictoryCountByQueryCriteria(DictoryQuery query) {
		return dictoryMapper.getDictoryCount(query);
	}

	public PageResult getDictoryList(int pageNo, int pageSize) {
		// 计算总数
		PageResult pager = new PageResult();
		DictoryQuery query = new DictoryQuery();
		int count = this.count(query);
		if (count == 0) {// 结果集为空
			pager.setPageSize(pageSize);
			return pager;
		}
		query.setOrderBy(" E.SORTNO asc");

		int start = pageSize * (pageNo - 1);
		List<Dictory> list = this.getDictorysByQueryCriteria(start, pageSize, query);
		pager.setResults(list);
		pager.setPageSize(pageSize);
		pager.setCurrentPageNo(pageNo);
		pager.setTotalRecordCount(count);

		return pager;
	}

	public List<Dictory> getDictoryList(long nodeId) {
		DictoryQuery query = new DictoryQuery();
		query.nodeId(nodeId);
		query.setOrderBy(" E.SORTNO asc ");
		return this.list(query);
	}

	public PageResult getDictoryList(long nodeId, int pageNo, int pageSize) {
		// 计算总数
		PageResult pager = new PageResult();
		DictoryQuery query = new DictoryQuery();
		query.nodeId(nodeId);
		int count = this.count(query);
		if (count == 0) {// 结果集为空
			pager.setPageSize(pageSize);
			return pager;
		}
		query.setOrderBy(" E.SORTNO asc");

		int start = pageSize * (pageNo - 1);
		List<Dictory> list = this.getDictorysByQueryCriteria(start, pageSize, query);
		pager.setResults(list);
		pager.setPageSize(pageSize);
		pager.setCurrentPageNo(pageNo);
		pager.setTotalRecordCount(count);

		return pager;
	}

	/**
	 * 返回某分类下的所有字典列表
	 * 
	 * @param nodeCode
	 * @return
	 */
	public List<Dictory> getDictoryList(String nodeCode) {
		SysTree tree = sysTreeService.getSysTreeByCode(nodeCode);
		if (tree == null) {
			return null;
		}
		return this.getAvailableDictoryList(tree.getId());
	}

	/**
	 * 返回某分类下的所有字典列表
	 * 
	 * @param category
	 * @return
	 */
	public List<Dictory> getDictoryListByCategory(String category) {
		return dictoryMapper.getDictoryListByCategory(category);
	}

	public List<Dictory> getDictorysByQueryCriteria(int start, int pageSize, DictoryQuery query) {
		RowBounds rowBounds = new RowBounds(start, pageSize);
        return sqlSessionTemplate.selectList("getDictories", query, rowBounds);
	}

	private List<Dictory> list(DictoryQuery query) {
        return dictoryMapper.getDictories(query);
	}

	@Transactional
	public void save(Dictory dictory) {
		if (dictory.getId() == 0) {
			dictory.setId(idGenerator.nextId());
			dictory.setCreateDate(new Date());
			dictory.setSort(1);
			dictoryMapper.insertDictory(dictory);

		} else {
			dictory.setUpdateDate(new Date());
			dictoryMapper.updateDictory(dictory);
			if (SystemConfig.getBoolean("use_query_cache")) {
				String cacheKey = "sys_dict_" + dictory.getId();
				CacheFactory.remove("dictory", cacheKey);
			}
		}
		if (SystemConfig.getBoolean("use_query_cache")) {
			String cacheKey = "sys_dicts_" + dictory.getNodeId();
			CacheFactory.remove("dictory", cacheKey);
		}
	}

	@javax.annotation.Resource
	public void setDictoryMapper(DictoryMapper dictoryMapper) {
		this.dictoryMapper = dictoryMapper;
	}

	@javax.annotation.Resource
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	@javax.annotation.Resource
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	@javax.annotation.Resource
	public void setSysTreeService(SysTreeService sysTreeService) {
		this.sysTreeService = sysTreeService;
	}

	@Transactional
	public boolean update(Dictory bean) {
		this.save(bean);
		return true;
	}

 

}
