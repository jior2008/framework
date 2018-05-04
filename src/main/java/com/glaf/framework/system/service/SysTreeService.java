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
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.glaf.framework.system.domain.SysTree;
import com.glaf.framework.system.query.SysTreeQuery;
import com.glaf.core.util.PageResult;

@Transactional(readOnly = true)
public interface SysTreeService {

	/**
	 * 保存
	 * 
	 * @param bean
	 *            SysTree
	 * @return boolean
	 */
	@Transactional
	boolean create(SysTree bean);

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
	 * 删除
	 * 
	 * @param bean
	 *            SysTree
	 * @return boolean
	 */
	@Transactional
	boolean delete(SysTree bean);

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
	SysTree findById(long id);

	/**
	 * 按名称查找对象
	 * 
	 * @param name
	 *            String
	 * @return SysTree
	 */
	SysTree findByName(String name);

	/**
	 * 获取全部列表
	 * 
	 * @return List
	 */
	List<SysTree> getAllSysTreeList();

	List<SysTree> getAvailableSysTrees(List<SysTree> list);

	/**
	 * 获取字典树型结构
	 * 
	 * @param query
	 * @return
	 */
	List<SysTree> getDictorySysTrees(SysTreeQuery query);

	/**
	 * 获取关联表树型结构
	 * 
	 * @param relationTable
	 *            表名
	 * @param relationColumn
	 *            关联字段名
	 * @param query
	 * @return
	 */
	List<SysTree> getRelationSysTrees(String relationTable, String relationColumn, SysTreeQuery query);

	/**
	 * 获取树型列表
	 * 
	 * @param parent
	 *            int
	 * @return List
	 */
	void getSysTree(List<SysTree> treeList, long parentId, int deep);

	/**
	 * 按树编号获取树节点
	 * 
	 * @param tree
	 * @return SysTree
	 */
	SysTree getSysTreeByCode(String code);

	/**
	 * 获取某个节点及其祖先
	 * 
	 * @param id
	 * @return
	 */
	SysTree getSysTreeByIdWithAncestor(long id);

	/**
	 * 根据查询参数获取记录总数
	 * 
	 * @return
	 */
	int getSysTreeCountByQueryCriteria(SysTreeQuery query);

	/**
	 * 获取全部列表
	 * 
	 * @param parentId
	 * 
	 * @return List
	 */
	List<SysTree> getSysTreeList(long parentId);

	/**
	 * 获取分页列表
	 * 
	 * @param parent
	 *            int
	 * @param pageNo
	 *            int
	 * @param pageSize
	 *            int
	 * @return
	 */
	PageResult getSysTreeList(long parentId, int pageNo, int pageSize);

	/**
	 * 获取全部列表
	 * 
	 * @param parent
	 * 
	 * @return List
	 */
	List<SysTree> getSysTreeListWithChildren(long parentId);

	/**
	 * 获取父节点列表，如:根目录>A>A1>A11
	 * 
	 * @param tree
	 * @param int
	 *            id
	 */
	void getSysTreeParent(List<SysTree> tree, long id);

	List<SysTree> getSysTrees(SysTreeQuery query);

	/**
	 * 根据查询参数获取一页的数据
	 * 
	 * @return
	 */
	List<SysTree> getSysTreesByQueryCriteria(int start, int pageSize, SysTreeQuery query);

	/**
	 * 获取树型列表
	 * 
	 * @param parent
	 *            int
	 * @return List
	 */
	void loadSysTrees(List<SysTree> treeList, long parentId, int deep);

	/**
	 * 更新
	 * 
	 * @param bean
	 *            SysTree
	 * @return boolean
	 */
	@Transactional
	boolean update(SysTree bean);

	/**
	 * 更新树的treeId字段
	 * 
	 * @param treeMap
	 */
	@Transactional
	void updateTreeIds();

	/**
	 * 更新指定树的treeId字段
	 * 
	 * @param treeMap
	 */
	@Transactional
	void updateTreeIds(Map<Long, String> treeMap);
}