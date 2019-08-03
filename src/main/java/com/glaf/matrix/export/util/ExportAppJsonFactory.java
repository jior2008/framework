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

package com.glaf.matrix.export.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glaf.core.util.DateUtils;
import com.glaf.matrix.export.domain.ExportApp;

/**
 * 
 * JSON工厂类
 *
 */
public class ExportAppJsonFactory {

	public static ExportApp jsonToObject(JSONObject jsonObject) {
		ExportApp model = new ExportApp();
		if (jsonObject.containsKey("id")) {
			model.setId(jsonObject.getString("id"));
		}
		if (jsonObject.containsKey("nodeId")) {
			model.setNodeId(jsonObject.getLong("nodeId"));
		}
		if (jsonObject.containsKey("deploymentId")) {
			model.setDeploymentId(jsonObject.getString("deploymentId"));
		}
		if (jsonObject.containsKey("title")) {
			model.setTitle(jsonObject.getString("title"));
		}

		if (jsonObject.containsKey("syncFlag")) {
			model.setSyncFlag(jsonObject.getString("syncFlag"));
		}
		if (jsonObject.containsKey("srcDatabaseId")) {
			model.setSrcDatabaseId(jsonObject.getLong("srcDatabaseId"));
		}

		if (jsonObject.containsKey("type")) {
			model.setType(jsonObject.getString("type"));
		}
		if (jsonObject.containsKey("active")) {
			model.setActive(jsonObject.getString("active"));
		}
		if (jsonObject.containsKey("allowRoles")) {
			model.setAllowRoles(jsonObject.getString("allowRoles"));
		}
		if (jsonObject.containsKey("templateId")) {
			model.setTemplateId(jsonObject.getString("templateId"));
		}
		if (jsonObject.containsKey("exportFileExpr")) {
			model.setExportFileExpr(jsonObject.getString("exportFileExpr"));
		}
		if (jsonObject.containsKey("exportPDFTool")) {
			model.setExportPDFTool(jsonObject.getString("exportPDFTool"));
		}
		if (jsonObject.containsKey("mergePDFFlag")) {
			model.setMergePDFFlag(jsonObject.getString("mergePDFFlag"));
		}
		if (jsonObject.containsKey("externalColumnsFlag")) {
			model.setExternalColumnsFlag(jsonObject.getString("externalColumnsFlag"));
		}
		if (jsonObject.containsKey("excelProcessChains")) {
			model.setExcelProcessChains(jsonObject.getString("excelProcessChains"));
		}
		if (jsonObject.containsKey("pageHeight")) {
			model.setPageHeight(jsonObject.getInteger("pageHeight"));
		}
		if (jsonObject.containsKey("pageNumPerSheet")) {
			model.setPageNumPerSheet(jsonObject.getInteger("pageNumPerSheet"));
		}
		if (jsonObject.containsKey("pageVarName")) {
			model.setPageVarName(jsonObject.getString("pageVarName"));
		}
		if (jsonObject.containsKey("historyFlag")) {
			model.setHistoryFlag(jsonObject.getString("historyFlag"));
		}
		if (jsonObject.containsKey("mulitiFlag")) {
			model.setMulitiFlag(jsonObject.getString("mulitiFlag"));
		}
		if (jsonObject.containsKey("enableSQLFlag")) {
			model.setEnableSQLFlag(jsonObject.getString("enableSQLFlag"));
		}
		if (jsonObject.containsKey("saveDataFlag")) {
			model.setSaveDataFlag(jsonObject.getString("saveDataFlag"));
		}
		if (jsonObject.containsKey("generateFlag")) {
			model.setGenerateFlag(jsonObject.getString("generateFlag"));
		}
		if (jsonObject.containsKey("genTime")) {
			model.setGenTime(jsonObject.getInteger("genTime"));
		}
		if (jsonObject.containsKey("parameterDatasetId")) {
			model.setParameterDatasetId(jsonObject.getString("parameterDatasetId"));
		}
		if (jsonObject.containsKey("outParameterColumns")) {
			model.setOutParameterColumns(jsonObject.getString("outParameterColumns"));
		}
		if (jsonObject.containsKey("parallelFlag")) {
			model.setParallelFlag(jsonObject.getString("parallelFlag"));
		}
		if (jsonObject.containsKey("shedulerFlag")) {
			model.setShedulerFlag(jsonObject.getString("shedulerFlag"));
		}
		if (jsonObject.containsKey("interval")) {
			model.setInterval(jsonObject.getInteger("interval"));
		}
		if (jsonObject.containsKey("sortNo")) {
			model.setSortNo(jsonObject.getInteger("sortNo"));
		}
		if (jsonObject.containsKey("createBy")) {
			model.setCreateBy(jsonObject.getString("createBy"));
		}
		if (jsonObject.containsKey("createTime")) {
			model.setCreateTime(jsonObject.getDate("createTime"));
		}
		if (jsonObject.containsKey("updateBy")) {
			model.setUpdateBy(jsonObject.getString("updateBy"));
		}
		if (jsonObject.containsKey("updateTime")) {
			model.setUpdateTime(jsonObject.getDate("updateTime"));
		}

		return model;
	}

	public static JSONObject toJsonObject(ExportApp model) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", model.getId());
		jsonObject.put("_id_", model.getId());
		jsonObject.put("_oid_", model.getId());
		jsonObject.put("nodeId", model.getNodeId());
		jsonObject.put("sortNo", model.getSortNo());

		if (model.getDeploymentId() != null) {
			jsonObject.put("deploymentId", model.getDeploymentId());
		}
		if (model.getTitle() != null) {
			jsonObject.put("title", model.getTitle());
		}

		if (model.getSyncFlag() != null) {
			jsonObject.put("syncFlag", model.getSyncFlag());
		}
		if (model.getSrcDatabaseId() != 0) {
			jsonObject.put("srcDatabaseId", model.getSrcDatabaseId());
		}

		if (model.getType() != null) {
			jsonObject.put("type", model.getType());
		}
		if (model.getActive() != null) {
			jsonObject.put("active", model.getActive());
		}
		if (model.getAllowRoles() != null) {
			jsonObject.put("allowRoles", model.getAllowRoles());
		}
		if (model.getTemplateId() != null) {
			jsonObject.put("templateId", model.getTemplateId());
		}
		if (model.getExportFileExpr() != null) {
			jsonObject.put("exportFileExpr", model.getExportFileExpr());
		}
		if (model.getExportPDFTool() != null) {
			jsonObject.put("exportPDFTool", model.getExportPDFTool());
		}
		if (model.getMergePDFFlag() != null) {
			jsonObject.put("mergePDFFlag", model.getMergePDFFlag());
		}
		if (model.getExternalColumnsFlag() != null) {
			jsonObject.put("externalColumnsFlag", model.getExternalColumnsFlag());
		}
		if (model.getExcelProcessChains() != null) {
			jsonObject.put("excelProcessChains", model.getExcelProcessChains());
		}
		jsonObject.put("pageHeight", model.getPageHeight());
		jsonObject.put("pageNumPerSheet", model.getPageNumPerSheet());
		if (model.getPageVarName() != null) {
			jsonObject.put("pageVarName", model.getPageVarName());
		}
		if (model.getHistoryFlag() != null) {
			jsonObject.put("historyFlag", model.getHistoryFlag());
		}
		if (model.getMulitiFlag() != null) {
			jsonObject.put("mulitiFlag", model.getMulitiFlag());
		}
		if (model.getEnableSQLFlag() != null) {
			jsonObject.put("enableSQLFlag", model.getEnableSQLFlag());
		}
		if (model.getSaveDataFlag() != null) {
			jsonObject.put("saveDataFlag", model.getSaveDataFlag());
		}
		if (model.getParameterDatasetId() != null) {
			jsonObject.put("parameterDatasetId", model.getParameterDatasetId());
		}
		if (model.getOutParameterColumns() != null) {
			jsonObject.put("outParameterColumns", model.getOutParameterColumns());
		}
		if (model.getParallelFlag() != null) {
			jsonObject.put("parallelFlag", model.getParallelFlag());
		}
		if (model.getShedulerFlag() != null) {
			jsonObject.put("shedulerFlag", model.getShedulerFlag());
		}
		jsonObject.put("interval", model.getInterval());
		
		if (model.getGenerateFlag() != null) {
			jsonObject.put("generateFlag", model.getGenerateFlag());
		} 
		jsonObject.put("genTime", model.getGenTime());

		if (model.getCreateBy() != null) {
			jsonObject.put("createBy", model.getCreateBy());
		}
		if (model.getCreateTime() != null) {
			jsonObject.put("createTime", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_date", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_datetime", DateUtils.getDateTime(model.getCreateTime()));
		}
		if (model.getUpdateBy() != null) {
			jsonObject.put("updateBy", model.getUpdateBy());
		}
		if (model.getUpdateTime() != null) {
			jsonObject.put("updateTime", DateUtils.getDate(model.getUpdateTime()));
			jsonObject.put("updateTime_date", DateUtils.getDate(model.getUpdateTime()));
			jsonObject.put("updateTime_datetime", DateUtils.getDateTime(model.getUpdateTime()));
		}
		return jsonObject;
	}

	public static ObjectNode toObjectNode(ExportApp model) {
		ObjectNode jsonObject = new ObjectMapper().createObjectNode();
		jsonObject.put("id", model.getId());
		jsonObject.put("_id_", model.getId());
		jsonObject.put("_oid_", model.getId());
		jsonObject.put("nodeId", model.getNodeId());
		jsonObject.put("sortNo", model.getSortNo());

		if (model.getDeploymentId() != null) {
			jsonObject.put("deploymentId", model.getDeploymentId());
		}
		if (model.getTitle() != null) {
			jsonObject.put("title", model.getTitle());
		}

		if (model.getSyncFlag() != null) {
			jsonObject.put("syncFlag", model.getSyncFlag());
		}
		if (model.getSrcDatabaseId() != 0) {
			jsonObject.put("srcDatabaseId", model.getSrcDatabaseId());
		}
		if (model.getType() != null) {
			jsonObject.put("type", model.getType());
		}
		if (model.getActive() != null) {
			jsonObject.put("active", model.getActive());
		}
		if (model.getAllowRoles() != null) {
			jsonObject.put("allowRoles", model.getAllowRoles());
		}
		if (model.getTemplateId() != null) {
			jsonObject.put("templateId", model.getTemplateId());
		}
		if (model.getExportFileExpr() != null) {
			jsonObject.put("exportFileExpr", model.getExportFileExpr());
		}
		if (model.getExportPDFTool() != null) {
			jsonObject.put("exportPDFTool", model.getExportPDFTool());
		}
		if (model.getMergePDFFlag() != null) {
			jsonObject.put("mergePDFFlag", model.getMergePDFFlag());
		}
		if (model.getExternalColumnsFlag() != null) {
			jsonObject.put("externalColumnsFlag", model.getExternalColumnsFlag());
		}
		if (model.getExcelProcessChains() != null) {
			jsonObject.put("excelProcessChains", model.getExcelProcessChains());
		}
		jsonObject.put("pageHeight", model.getPageHeight());
		jsonObject.put("pageNumPerSheet", model.getPageNumPerSheet());
		if (model.getPageVarName() != null) {
			jsonObject.put("pageVarName", model.getPageVarName());
		}
		if (model.getHistoryFlag() != null) {
			jsonObject.put("historyFlag", model.getHistoryFlag());
		}
		if (model.getMulitiFlag() != null) {
			jsonObject.put("mulitiFlag", model.getMulitiFlag());
		}
		if (model.getEnableSQLFlag() != null) {
			jsonObject.put("enableSQLFlag", model.getEnableSQLFlag());
		}
		if (model.getSaveDataFlag() != null) {
			jsonObject.put("saveDataFlag", model.getSaveDataFlag());
		}
		if (model.getParameterDatasetId() != null) {
			jsonObject.put("parameterDatasetId", model.getParameterDatasetId());
		}
		if (model.getOutParameterColumns() != null) {
			jsonObject.put("outParameterColumns", model.getOutParameterColumns());
		}
		if (model.getParallelFlag() != null) {
			jsonObject.put("parallelFlag", model.getParallelFlag());
		}
		if (model.getShedulerFlag() != null) {
			jsonObject.put("shedulerFlag", model.getShedulerFlag());
		}
		jsonObject.put("interval", model.getInterval());
		if (model.getGenerateFlag() != null) {
			jsonObject.put("generateFlag", model.getGenerateFlag());
		} 
		jsonObject.put("genTime", model.getGenTime());
		if (model.getCreateBy() != null) {
			jsonObject.put("createBy", model.getCreateBy());
		}
		if (model.getCreateTime() != null) {
			jsonObject.put("createTime", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_date", DateUtils.getDate(model.getCreateTime()));
			jsonObject.put("createTime_datetime", DateUtils.getDateTime(model.getCreateTime()));
		}
		if (model.getUpdateBy() != null) {
			jsonObject.put("updateBy", model.getUpdateBy());
		}
		if (model.getUpdateTime() != null) {
			jsonObject.put("updateTime", DateUtils.getDate(model.getUpdateTime()));
			jsonObject.put("updateTime_date", DateUtils.getDate(model.getUpdateTime()));
			jsonObject.put("updateTime_datetime", DateUtils.getDateTime(model.getUpdateTime()));
		}
		return jsonObject;
	}

	public static JSONArray listToArray(java.util.List<ExportApp> list) {
		JSONArray array = new JSONArray();
		if (list != null && !list.isEmpty()) {
			for (ExportApp model : list) {
				JSONObject jsonObject = model.toJsonObject();
				array.add(jsonObject);
			}
		}
		return array;
	}

	public static java.util.List<ExportApp> arrayToList(JSONArray array) {
		java.util.List<ExportApp> list = new java.util.ArrayList<ExportApp>();
		for (int i = 0, len = array.size(); i < len; i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			ExportApp model = jsonToObject(jsonObject);
			list.add(model);
		}
		return list;
	}

	private ExportAppJsonFactory() {

	}

}
