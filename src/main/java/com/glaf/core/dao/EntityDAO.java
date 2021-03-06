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

package com.glaf.core.dao;

import com.glaf.core.id.IdBlock;

import java.util.List;

public interface EntityDAO {

	/**
	 * 删除记录
	 * 
	 * @param statementId
	 * @param parameterObject
	 */
	void delete(String statementId, Object parameterObject);

	/**
	 * 删除多条记录
	 * 
	 * @param statementId
	 * @param rowIds
	 */
	void deleteAll(String statementId, List<Object> rowIds);

	/**
	 * 根据记录主键删除记录
	 * 
	 * @param statementId
	 * @param rowId
	 */
	void deleteById(String statementId, Object rowId);

	/**
	 * 根据主键获取记录
	 * 
	 * @param statementId
	 * @param parameterObject
	 * @return
	 */
	Object getById(String statementId, Object parameterObject);

	/**
	 * 获取总记录数
	 * 
	 * @param statementId
	 * @param parameterObject
	 * @return
	 */
	int getCount(String statementId, Object parameterObject);

	/**
	 * 获取数据集
	 * 
	 * @param statementId
	 * @param parameterObject
	 * @return
	 */
	List<Object> getList(String statementId, Object parameterObject);

	/**
	 * 获取下一条记录编号
	 * 
	 * @param tablename
	 *            表名称
	 * @param idColumn
	 *            主键列名
	 * @return
	 */
	long getMaxId(String tablename, String idColumn);

	/**
	 * 获取单个对象
	 * 
	 * @param statementId
	 * @param parameterObject
	 * @return
	 */
	Object getSingleObject(String statementId, Object parameterObject);

	/**
	 * 获取下一条记录编号
	 * 
	 * @return
	 */
	int getTableUserMaxId(String tablename, String idColumn, String createBy);

	/**
	 * 插入一条记录
	 * 
	 * @param statementId
	 * @param parameterObject
	 */
	void insert(String statementId, Object parameterObject);

	/**
	 * 插入多条记录
	 * 
	 * @param statementId
	 * @param rows
	 */
	void insertAll(String statementId, List<Object> rows);

	/**
	 * 获取下一条记录编号
	 * 
	 * @return
	 */
	IdBlock nextDbidBlock();

	/**
	 * 获取下一条记录编号
	 * 
	 * @param name
	 *            名称
	 * @return
	 */
	IdBlock nextDbidBlock(String name);

	void setConnection(java.sql.Connection connection);

	/**
	 * 修改一条记录
	 * 
	 * @param statementId
	 * @param parameterObject
	 */
	void update(String statementId, Object parameterObject);

	/**
	 * 修改多条记录
	 * 
	 * @param statementId
	 * @param rows
	 *
	 */
	void updateAll(String statementId, List<Object> rows);

}