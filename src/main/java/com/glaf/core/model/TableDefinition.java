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

package com.glaf.core.model;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glaf.core.factory.ColumnDefinitionJsonFactory;
import com.glaf.core.factory.TableDefinitionJsonFactory;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 

/**
 * 数据表定义
 * 
 * 
 */

public class TableDefinition implements java.io.Serializable, java.lang.Comparable<TableDefinition> {
	private static final long serialVersionUID = 1L;

	private String tableId;

	/**
	 * 表名
	 */
	private String tableName;

	private int addType;

	private String dbType;

	/**
	 * 聚合主键列集
	 */
	private String aggregationKey;

	private String className;

	private int columnQty;

	private List<ColumnDefinition> columns = new java.util.ArrayList<ColumnDefinition>();

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 级联删除
	 */
	private int deleteCascade;

	/**
	 * 是否删除抓取数据
	 */
	private String deleteFetch;

	private int deleteFlag;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * 显示类型 form,grid,tree,treegrid
	 */
	private String displayType;

	/**
	 * 标题
	 */
	private String englishTitle;

	private String entityName;

	private ColumnDefinition idColumn;

	private List<ColumnDefinition> idColumns;

	/**
	 * 级联插入
	 */
	private int insertCascade;

	private boolean insertOnly;

	private boolean updateAllowed = true;

	/**
	 * 是否从表(Y-从表，N-不是从表)
	 */
	private String isSubTable;

	/**
	 * 是否需要JBPM工作流支持
	 */
	private boolean jbpmSupport;

	/**
	 * 是否锁定
	 */
	private int locked;

	/**
	 * 模块名称
	 */
	private String moduleName;

	private String packageName;

	private String primaryKey;

	/**
	 * 修订版本
	 */
	private int revision;

	private int sortNo;

	private String sysnum;

	private String systemFlag;

	private String name;

	/**
	 * 附件标识 0-无，1-1个附件，2-多个附件
	 */
	private String attachmentFlag;

	/**
	 * 附件允许的扩展名
	 */
	private String attachmentExts;

	/**
	 * 附件大小
	 */
	private int attachmentSize;

	/**
	 * 审核标记
	 */
	private String auditFlag;

	/**
	 * 树型结构标识， Y-树型，N-非树型
	 */
	private String treeFlag;

	/**
	 * 权限标识
	 */
	private String privilegeFlag;

	/**
	 * 是否临时表
	 */
	private String temporaryFlag;

	/**
	 * 标题
	 */
	private String title;

	private String topId;

	/**
	 * 是否树型结构
	 */
	private boolean treeSupport;

	/**
	 * 表类型
	 */
	private String type;

	/**
	 * 级联更新
	 */
	private int updateCascade;

	private Long nodeId;

	public TableDefinition() {

	}

	public void addColumn(ColumnDefinition column) {
		if (columns == null) {
			columns = new java.util.ArrayList<ColumnDefinition>();
		}
		if (!columns.contains(column)) {
			columns.add(column);
		}
	}

	public void addField(ColumnDefinition field) {
		if (columns == null) {
			columns = new java.util.ArrayList<ColumnDefinition>();
		}
		JSONObject jsonObject = field.toJsonObject();
		ColumnDefinition column = ColumnDefinitionJsonFactory.jsonToObject(jsonObject);
		columns.add(column);
	}

	public void addIdColumn(ColumnDefinition column) {
		if (idColumns == null) {
			idColumns = new java.util.ArrayList<ColumnDefinition>();
		}
		if (!idColumns.contains(column)) {
			idColumns.add(column);
		}
	}

	public int compareTo(TableDefinition o) {
		if (o == null) {
			return -1;
		}

		int l = this.sortNo - o.getSortNo();

		int ret = 0;

		if (l > 0) {
			ret = 1;
		} else if (l < 0) {
			ret = -1;
		}
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableDefinition other = (TableDefinition) obj;
		if (tableName == null) {
            return other.tableName == null;
		} else return tableName.equals(other.tableName);
    }

	public int getAddType() {
		return addType;
	}

	public String getAggregationKey() {
		return aggregationKey;
	}

	public String getAttachmentExts() {
		return attachmentExts;
	}

	public String getAttachmentFlag() {
		return attachmentFlag;
	}

	public int getAttachmentSize() {
		return attachmentSize;
	}

	public String getAuditFlag() {
		return auditFlag;
	}

	public String getClassName() {
		if (className != null) {
			return className;
		}
		if (getPackageName() != null && getEntityName() != null) {
			return getPackageName() + "." + getEntityName();
		}
		return this.getEntityName();
	}

	public int getColumnQty() {
		return columnQty;
	}

	public List<ColumnDefinition> getColumns() {
		return columns;
	}

	public String getCreateBy() {
		return createBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public String getDbType() {
		return dbType;
	}

	public int getDeleteCascade() {
		return deleteCascade;
	}

	public String getDeleteFetch() {
		return deleteFetch;
	}

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayType() {
		return displayType;
	}

	public String getEnglishTitle() {
		return englishTitle;
	}

	public String getEntityName() {
		return entityName;
	}

	public Map<String, ColumnDefinition> getFields() {
		Map<String, ColumnDefinition> fieldMap = new LinkedHashMap<String, ColumnDefinition>();
		if (columns != null && !columns.isEmpty()) {
			for (ColumnDefinition column : columns) {
				fieldMap.put(column.getName(), column);
			}
		}
		return fieldMap;
	}

	public ColumnDefinition getIdColumn() {
		return idColumn;
	}

	public List<ColumnDefinition> getIdColumns() {
		return idColumns;
	}

	public ColumnDefinition getIdField() {
		return idColumn;
	}

	public int getInsertCascade() {
		return insertCascade;
	}

	public String getIsSubTable() {
		return isSubTable;
	}

	public int getLocked() {
		return locked;
	}

	public String getModuleName() {
		if (moduleName == null) {
			moduleName = "apps";
		}
		return moduleName;
	}

	public String getName() {
		return name;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public String getPrivilegeFlag() {
		return privilegeFlag;
	}

	public int getRevision() {
		return revision;
	}

	public int getSortNo() {
		return sortNo;
	}

	public String getSysnum() {
		return sysnum;
	}

	public String getSystemFlag() {
		return systemFlag;
	}

	public String getTableId() {
		return tableId;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTemporaryFlag() {
		return temporaryFlag;
	}

	public String getTitle() {
		return title;
	}

	public String getTopId() {
		return topId;
	}

	public String getTreeFlag() {
		return treeFlag;
	}

	public String getType() {
		return type;
	}

	public int getUpdateCascade() {
		return updateCascade;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	public boolean isInsertOnly() {
		return insertOnly;
	}

	public boolean isJbpmSupport() {
		return jbpmSupport;
	}

	public boolean isTreeSupport() {
		return treeSupport;
	}

	public boolean isUpdateAllowed() {
		return updateAllowed;
	}

	public TableDefinition jsonToObject(JSONObject jsonObject) {
		return TableDefinitionJsonFactory.jsonToObject(jsonObject);
	}

	public void setAddType(int addType) {
		this.addType = addType;
	}

	public void setAggregationKey(String aggregationKey) {
		this.aggregationKey = aggregationKey;
	}

	public void setAttachmentExts(String attachmentExts) {
		this.attachmentExts = attachmentExts;
	}

	public void setAttachmentFlag(String attachmentFlag) {
		this.attachmentFlag = attachmentFlag;
	}

	public void setAttachmentSize(int attachmentSize) {
		this.attachmentSize = attachmentSize;
	}

	public void setAuditFlag(String auditFlag) {
		this.auditFlag = auditFlag;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setColumnQty(int columnQty) {
		this.columnQty = columnQty;
	}

	public void setColumns(List<ColumnDefinition> columns) {
		this.columns = columns;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setDeleteCascade(int deleteCascade) {
		this.deleteCascade = deleteCascade;
	}

	public void setDeleteFetch(String deleteFetch) {
		this.deleteFetch = deleteFetch;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public void setEnglishTitle(String englishTitle) {
		this.englishTitle = englishTitle;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public void setIdColumn(ColumnDefinition idColumn) {
		if (idColumn != null) {
			this.idColumn = idColumn;
			this.idColumn.setPrimaryKey(true);
			this.addColumn(idColumn);
		}
	}

	public void setIdColumns(List<ColumnDefinition> idColumns) {
		this.idColumns = idColumns;
	}

	public void setIdField(ColumnDefinition idField) {
		JSONObject jsonObject = idField.toJsonObject();
		this.idColumn = ColumnDefinitionJsonFactory.jsonToObject(jsonObject);
		idColumn.setPrimaryKey(true);
	}

	public void setInsertCascade(int insertCascade) {
		this.insertCascade = insertCascade;
	}

	public void setInsertOnly(boolean insertOnly) {
		this.insertOnly = insertOnly;
	}

	public void setIsSubTable(String isSubTable) {
		this.isSubTable = isSubTable;
	}

	public void setJbpmSupport(boolean jbpmSupport) {
		this.jbpmSupport = jbpmSupport;
	}

	public void setLocked(int locked) {
		this.locked = locked;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setPrivilegeFlag(String privilegeFlag) {
		this.privilegeFlag = privilegeFlag;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}

	public void setSysnum(String sysnum) {
		this.sysnum = sysnum;
	}

	public void setSystemFlag(String systemFlag) {
		this.systemFlag = systemFlag;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setTemporaryFlag(String temporaryFlag) {
		this.temporaryFlag = temporaryFlag;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTopId(String topId) {
		this.topId = topId;
	}

	public void setTreeFlag(String treeFlag) {
		this.treeFlag = treeFlag;
	}

	public void setTreeSupport(boolean treeSupport) {
		this.treeSupport = treeSupport;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUpdateAllowed(boolean updateAllowed) {
		this.updateAllowed = updateAllowed;
	}

	public void setUpdateCascade(int updateCascade) {
		this.updateCascade = updateCascade;
	}

	public JSONObject toJsonObject() {
		return TableDefinitionJsonFactory.toJsonObject(this);
	}

	public ObjectNode toObjectNode() {
		return TableDefinitionJsonFactory.toObjectNode(this);
	}

}