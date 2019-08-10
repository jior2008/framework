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

package com.glaf.framework.service.impl;

import com.glaf.core.base.TableModel;
import com.glaf.core.service.ITableDataService;
import com.glaf.framework.mapper.TableDataMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("tableDataService")
@Transactional
public class TableDataServiceImpl implements ITableDataService {
	protected final static Log logger = LogFactory.getLog(TableDataServiceImpl.class);

	private TableDataMapper tableDataMapper;

	public TableDataServiceImpl() {

	}

	/**
	 * 删除数据
	 * 
	 * @param model
	 */
	@Transactional
	public void deleteTableData(TableModel model) {
		if (StringUtils.isNotEmpty(model.getTableName()) && model.getColumns() != null
				&& !model.getColumns().isEmpty()) {
			if (model.getTableName() != null) {
				model.setTableName(model.getTableName().toUpperCase());
			}
			tableDataMapper.deleteTableData(model);
		}
	}

	/**
	 * 删除数据
	 * 
	 * @param rows
	 */
	@Transactional
	public void deleteTableDataList(List<TableModel> rows) {
		if (rows != null && !rows.isEmpty()) {
			for (TableModel tableData : rows) {
				this.deleteTableData(tableData);
			}
		}
	}

	@Transactional
	public void insertTableData(TableModel tableModel) {
		if (tableModel.getTableName() != null) {
			tableModel.setTableName(tableModel.getTableName().toUpperCase());
		}
		tableDataMapper.insertTableData(tableModel);
	}


	@javax.annotation.Resource
	public void setTableDataMapper(TableDataMapper tableDataMapper) {
		this.tableDataMapper = tableDataMapper;
	}

	@Transactional
	public void updateTableData(TableModel model) {
		if (model.getTableName() != null) {
			model.setTableName(model.getTableName().toUpperCase());
		}
		tableDataMapper.updateTableDataByPrimaryKey(model);
	}

	@Transactional
	public void updateTableDataByWhere(TableModel model) {
		if (model.getTableName() != null) {
			model.setTableName(model.getTableName().toUpperCase());
		}
		tableDataMapper.updateTableData(model);
	}

}