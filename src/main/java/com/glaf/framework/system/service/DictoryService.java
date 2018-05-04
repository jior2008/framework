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

package com.glaf.framework.system.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.glaf.core.util.PageResult;
import com.glaf.framework.system.domain.Dictory;
import com.glaf.framework.system.query.DictoryQuery;

@Transactional(readOnly = true)
public interface DictoryService {

	/**
	 * 保存
	 * 
	 * @param bean
	 *            Dictory
	 * @return boolean
	 */
	@Transactional
	boolean create(Dictory bean);

	/**
	 * 删除
	 * 
	 * @param bean
	 *            Dictory
	 * @return boolean
	 */
	@Transactional
	boolean delete(Dictory bean);

	/**
	 * 删除
	 * 
	 * @param id
	 *            int
	 * @return boolean
	 */
	@Transactional
	boolean delete(long id);

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@Transactional
	boolean deleteAll(long[] ids);

	/**
	 * 获取对象
	 * 
	 * @param id
	 * @return
	 */
	Dictory find(long id);

	/**
	 * 返回某分类下的所有字典列表
	 * 
	 * @param parent
	 * @return
	 */
	List<Dictory> getAvailableDictoryList(long parent);

	/**
	 * 根据ID拿code
	 * 
	 * @param id
	 * @return
	 */
	String getCodeById(long id);

	List<Dictory> getDictories(DictoryQuery query);

	List<Dictory> getDictories(String codeLike);

	/**
	 * 根据查询参数获取记录总数
	 * 
	 * @return
	 */
	int getDictoryCountByQueryCriteria(DictoryQuery query);

	/**
	 * 获取分页列表
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	PageResult getDictoryList(int pageNo, int pageSize);

	/**
	 * 返回某分类下的所有字典列表
	 * 
	 * @param parent
	 * @return
	 */
	List<Dictory> getDictoryList(long parent);

	/**
	 * 按类型号搜索列表
	 * 
	 * @param parent
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	PageResult getDictoryList(long parent, int pageNo, int pageSize);

	/**
	 * 返回某分类下的所有字典列表
	 * 
	 * @param nodeCode
	 * @return
	 */
	List<Dictory> getDictoryList(String nodeCode);

	/**
	 * 返回某分类下的所有字典列表
	 * 
	 * @param category
	 * @return
	 */
	List<Dictory> getDictoryListByCategory(String category);

	/**
	 * 根据查询参数获取一页的数据
	 * 
	 * @return
	 */
	List<Dictory> getDictorysByQueryCriteria(int start, int pageSize, DictoryQuery query);

	/**
	 * 更新
	 * 
	 * @param bean
	 *            Dictory
	 * @return boolean
	 */
	@Transactional
	boolean update(Dictory bean);
}