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

package com.glaf.matrix.data.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glaf.core.util.DateUtils;
import com.glaf.matrix.data.domain.TableSysPermission;

/**
 * 
 * JSON工厂类
 *
 */
public class TableSysPermissionJsonFactory {

	public static java.util.List<TableSysPermission> arrayToList(JSONArray array) {
		java.util.List<TableSysPermission> list = new java.util.ArrayList<TableSysPermission>();
		for (int i = 0, len = array.size(); i < len; i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			TableSysPermission model = jsonToObject(jsonObject);
			list.add(model);
		}
		return list;
	}

	public static TableSysPermission jsonToObject(JSONObject jsonObject) {
		TableSysPermission model = new TableSysPermission();
		if (jsonObject.containsKey("id")) {
			model.setId(jsonObject.getString("id"));
		}
		if (jsonObject.containsKey("tableId")) {
			model.setTableId(jsonObject.getString("tableId"));
		}
		if (jsonObject.containsKey("TableName")) {
			model.setTableName(jsonObject.getString("TableName"));
		}
		if (jsonObject.containsKey("grantee")) {
			model.setGrantee(jsonObject.getString("grantee"));
		}
		if (jsonObject.containsKey("granteeType")) {
			model.setGranteeType(jsonObject.getString("granteeType"));
		}
		if (jsonObject.containsKey("privilege")) {
			model.setPrivilege(jsonObject.getString("privilege"));
		}
		if (jsonObject.containsKey("type")) {
			model.setType(jsonObject.getString("type"));
		}
		if (jsonObject.containsKey("locked")) {
			model.setLocked(jsonObject.getInteger("locked"));
		}
		if (jsonObject.containsKey("createBy")) {
			model.setCreateBy(jsonObject.getString("createBy"));
		}
		if (jsonObject.containsKey("createTime")) {
			model.setCreateTime(jsonObject.getDate("createTime"));
		}

		return model;
	}

	public static JSONArray listToArray(java.util.List<TableSysPermission> list) {
		JSONArray array = new JSONArray();
		if (list != null && !list.isEmpty()) {
			for (TableSysPermission model : list) {
				JSONObject jsonObject = model.toJsonObject();
				array.add(jsonObject);
			}
		}
		return array;
	}

	public static JSONObject toJsonObject(TableSysPermission model) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", model.getId());
		jsonObject.put("_id_", model.getId());
		jsonObject.put("_oid_", model.getId());
		if (model.getTableId() != null) {
			jsonObject.put("tableId", model.getTableId());
		}
		if (model.getTableName() != null) {
			jsonObject.put("TableName", model.getTableName());
		}
		if (model.getGrantee() != null) {
			jsonObject.put("grantee", model.getGrantee());
		}
		if (model.getGranteeType() != null) {
			jsonObject.put("granteeType", model.getGranteeType());
		}
		if (model.getPrivilege() != null) {
			jsonObject.put("privilege", model.getPrivilege());
		}
		if (model.getType() != null) {
			jsonObject.put("type", model.getType());
		}
		jsonObject.put("locked", model.getLocked());
		if (model.getCreateBy() != null) {
			jsonObject.put("createBy", model.getCreateBy());
		}
		if (model.getCreateTime() != null) {
			jsonObject.put("createTime", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_date", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_datetime", DateUtils.getDateTime(model.getCreateTime()));
		}
		return jsonObject;
	}

	public static ObjectNode toObjectNode(TableSysPermission model) {
		ObjectNode jsonObject = new ObjectMapper().createObjectNode();
		jsonObject.put("id", model.getId());
		jsonObject.put("_id_", model.getId());
		jsonObject.put("_oid_", model.getId());
		if (model.getTableId() != null) {
			jsonObject.put("tableId", model.getTableId());
		}
		if (model.getTableName() != null) {
			jsonObject.put("TableName", model.getTableName());
		}
		if (model.getGrantee() != null) {
			jsonObject.put("grantee", model.getGrantee());
		}
		if (model.getGranteeType() != null) {
			jsonObject.put("granteeType", model.getGranteeType());
		}
		if (model.getPrivilege() != null) {
			jsonObject.put("privilege", model.getPrivilege());
		}
		if (model.getType() != null) {
			jsonObject.put("type", model.getType());
		}
		jsonObject.put("locked", model.getLocked());
		if (model.getCreateBy() != null) {
			jsonObject.put("createBy", model.getCreateBy());
		}
		if (model.getCreateTime() != null) {
			jsonObject.put("createTime", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_date", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_datetime", DateUtils.getDateTime(model.getCreateTime()));
		}
		return jsonObject;
	}

	private TableSysPermissionJsonFactory() {

	}

}
