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

package com.glaf.core.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.util.StringTools;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

public class TableModel implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String aggregationKey;

	private Collection<String> aggregationKeys = new java.util.ArrayList<String>();

	/**
	 * 批处理的大小
	 */
	private int batchSize;

	private List<ColumnModel> columns = new java.util.ArrayList<ColumnModel>();

	private String dbType;

	/**
	 * 英文标题
	 */
	private String englishTitle;

	/**
	 * 实体名称
	 */
	private String entityName;

	/**
	 * 需要排除的行列表
	 */
	private List<String> excludes = new java.util.ArrayList<String>();

	/**
	 * 文件前缀
	 */
	private String filePrefix;

	private ColumnModel idColumn;

	/**
	 * 是否插入
	 */
	private boolean insertOnly;

	private boolean updateAllowed = true;

	/**
	 * 合法数据的最小长度
	 */
	private int minLength;

	/**
	 * Java 包名
	 */
	private String packageName;

	/**
	 * 自行提供的解析器类名
	 */
	private String parseClass;

	/**
	 * 解析类型,csv,text,xls
	 */
	private String parseType;

	/**
	 * 物理表的主键字段名称
	 */
	private String primaryKey;

	/**
	 * 分隔符
	 */
	private String split;

	private String sql;

	/**
	 * 解析类型 json,xls,xml
	 */
	private String exportType;

	/**
	 * 开始行数,从1开始
	 */
	private int startRow;

	/**
	 * 最后跳过行数(不需要处理的footer信息行数)
	 */
	private int stopSkipRow;

	/**
	 * 停止解析字符
	 */
	private String stopWord;

	/**
	 * 数据库表名称
	 */
	private String tableName;

	/**
	 * 标题
	 */
	private String title;

	private String sortColumnName;

	private String sortOrder;

	private String orderBy;

	/**
	 * 当前操作用户
	 */
	private String actorId;

	/**
	 * where查询列
	 */
	private List<ColumnModel> whereColumns = new java.util.ArrayList<ColumnModel>();

	private Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * 预处理程序
	 */
	private List<String> statements = new java.util.ArrayList<String>();

	public TableModel() {

	}

	public void addAttribute(String key, String value) {
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}
		attributes.put(key, value);
	}

	public void addCollectionColumn(String columnName, Collection<Object> collection) {
		if (columns == null) {
			columns = new java.util.ArrayList<ColumnModel>();
		}
		ColumnModel column = new ColumnModel();
		column.setColumnName(columnName);
		column.setJavaType("Collection");
		column.setCollectionValues(collection);
		column.setValue(collection);
		column.setTable(this);
		if (!columns.contains(column)) {
			columns.add(column);
		}
	}

	public void addColumn(ColumnModel column) {
		if (columns == null) {
			columns = new java.util.ArrayList<ColumnModel>();
		}
		if (!columns.contains(column)) {
			column.setTable(this);
			columns.add(column);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private void addColumn(String columnName, String javaType, Object value) {
		if (columns == null) {
			columns = new java.util.ArrayList<ColumnModel>();
		}
		ColumnModel column = new ColumnModel();
		column.setColumnName(columnName);
		column.setJavaType(javaType);
		if (value instanceof Collection) {
			column.setCollectionValues((Collection) value);
		}
		column.setValue(value);
		column.setTable(this);
		if (!columns.contains(column)) {
			columns.add(column);
		}
	}

	public void addDateColumn(String columnName, Date value) {
		this.addColumn(columnName, "Date", value);
	}

	public void addDoubleColumn(String columnName, Double value) {
		this.addColumn(columnName, "Double", value);
	}

	public void addExclude(String exclude) {
		if (excludes == null) {
			excludes = new java.util.ArrayList<String>();
		}
		excludes.add(exclude);
	}

	public void addIntegerColumn(String columnName, Integer value) {
		this.addColumn(columnName, "Integer", value);
	}

	public void addLongColumn(String columnName, Long value) {
		this.addColumn(columnName, "Long", value);
	}

	public void addStatement(String statement) {
		if (statements == null) {
			statements = new java.util.ArrayList<String>();
		}
		statements.add(statement);
	}

	public void addStringColumn(String columnName, String value) {
		this.addColumn(columnName, "String", value);
	}

	public void addWhereColumn(ColumnModel column) {
		if (whereColumns == null) {
			whereColumns = new java.util.ArrayList<ColumnModel>();
		}
		if (!whereColumns.contains(column)) {
			column.setTable(this);
			whereColumns.add(column);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableModel other = (TableModel) obj;
		if (tableName == null) {
            return other.tableName == null;
		} else return tableName.equals(other.tableName);
    }

	private String getActorId() {
		return actorId;
	}

	public String getAggregationKey() {
		return aggregationKey;
	}

	public Collection<String> getAggregationKeys() {
		if (aggregationKeys == null) {
			aggregationKeys = new HashSet<String>();
		}
		if (aggregationKeys.isEmpty()) {
			if (StringUtils.isNotEmpty(aggregationKey)) {
				aggregationKeys = StringTools.stringToCollection(aggregationKey);
			}
		}
		return aggregationKeys;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public String getDbType() {
		return dbType;
	}

	public String getEnglishTitle() {
		return englishTitle;
	}

	public String getEntityName() {
		return entityName;
	}

	public List<String> getExcludes() {
		if (excludes == null) {
			excludes = new java.util.ArrayList<String>();
		}
		return excludes;
	}

	public String getExportType() {
		return exportType;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public ColumnModel getIdColumn() {
		return idColumn;
	}

	public int getMinLength() {
		return minLength;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getParseClass() {
		return parseClass;
	}

	public String getParseType() {
		return parseType;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public String getSortColumnName() {
		return sortColumnName;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public String getSplit() {
		return split;
	}

	public String getSql() {
		return sql;
	}

	public int getStartRow() {
		return startRow;
	}

	public List<String> getStatements() {
		return statements;
	}

	public int getStopSkipRow() {
		return stopSkipRow;
	}

	public String getStopWord() {
		return stopWord;
	}

	public String getTableName() {
		if (tableName != null) {
			tableName = tableName.trim().toUpperCase();
		}
		return tableName;
	}

	public String getTitle() {
		return title;
	}

	public List<ColumnModel> getWhereColumns() {
		return whereColumns;
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

	public boolean isUpdateAllowed() {
		return updateAllowed;
	}

	public void removeColumn(ColumnModel column) {
		if (columns != null) {
			column.setTable(null);
			columns.remove(column);
		}
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public void setAggregationKey(String aggregationKey) {
		this.aggregationKey = aggregationKey;
	}

	public void setAggregationKeys(Collection<String> aggregationKeys) {
		this.aggregationKeys = aggregationKeys;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setEnglishTitle(String englishTitle) {
		this.englishTitle = englishTitle;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public void setIdColumn(ColumnModel idColumn) {
		this.idColumn = idColumn;
	}

	public void setInsertOnly(boolean insertOnly) {
		this.insertOnly = insertOnly;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setParseClass(String parseClass) {
		this.parseClass = parseClass;
	}

	public void setParseType(String parseType) {
		this.parseType = parseType;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setSortColumnName(String sortColumnName) {
		this.sortColumnName = sortColumnName;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public void setStatements(List<String> statements) {
		this.statements = statements;
	}

	public void setStopSkipRow(int stopSkipRow) {
		this.stopSkipRow = stopSkipRow;
	}

	public void setStopWord(String stopWord) {
		this.stopWord = stopWord;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUpdateAllowed(boolean updateAllowed) {
		this.updateAllowed = updateAllowed;
	}

	public void setWhereColumns(List<ColumnModel> whereColumns) {
		this.whereColumns = whereColumns;
	}

	public JSONObject toJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tableName", this.getTableName());
		jsonObject.put("actorId", this.getActorId());
		if (idColumn != null) {
			jsonObject.put("id", idColumn.toJsonObject());
		}
		if (columns != null && !columns.isEmpty()) {
			JSONArray array = new JSONArray();
			for (ColumnModel col : columns) {
				array.add(col.toJsonObject());
			}
			jsonObject.put("columns", array);
		}

		return jsonObject;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}