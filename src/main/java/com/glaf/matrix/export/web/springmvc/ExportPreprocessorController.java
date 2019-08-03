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

package com.glaf.matrix.export.web.springmvc;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.glaf.core.util.RequestUtils;
import com.glaf.core.util.ResponseUtils;
import com.glaf.matrix.data.domain.DataItemDefinition;
import com.glaf.matrix.data.service.DataItemDefinitionService;
import com.glaf.matrix.data.util.DataItemFactory;
import com.glaf.matrix.export.domain.ExportPreprocessor;
import com.glaf.matrix.export.query.ExportPreprocessorQuery;
import com.glaf.matrix.export.service.ExportPreprocessorService;

/**
 * 
 * SpringMVC控制器
 *
 */

@Controller("/sys/exportPreprocessor")
@RequestMapping("/sys/exportPreprocessor")
public class ExportPreprocessorController {
	protected static final Log logger = LogFactory.getLog(ExportPreprocessorController.class);

	protected DataItemDefinitionService dataItemDefinitionService;

	protected ExportPreprocessorService exportPreprocessorService;

	public ExportPreprocessorController() {

	}

	@RequestMapping("/edit")
	public ModelAndView edit(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		JSONArray array = null;
		String currentType = null;
		String code = request.getParameter("code");
		String expId = request.getParameter("expId");
		String currentStep = request.getParameter("currentStep");
		if (StringUtils.isNotEmpty(expId) && StringUtils.isNotEmpty(currentStep) && StringUtils.isNotEmpty(code)) {
			array = DataItemFactory.getJSONArray(code);
			request.setAttribute("array", array);
			DataItemDefinition item = dataItemDefinitionService.getDataItemDefinitionByCode(code);
			if (item != null) {
				request.setAttribute("item", item);
				currentType = item.getType();
				request.setAttribute("currentType", currentType);
				ExportPreprocessorQuery query = new ExportPreprocessorQuery();
				query.expId(expId);
				query.currentStep(currentStep);
				query.currentType(currentType);
				List<ExportPreprocessor> rows = exportPreprocessorService.list(query);
				request.setAttribute("rows", rows);
			}
		}

		List<DataItemDefinition> definitions = dataItemDefinitionService.getDataItemDefinitions("synthetic");
		request.setAttribute("definitions", definitions);

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		return new ModelAndView("/matrix/exportPreprocessor/edit", modelMap);
	}

	@ResponseBody
	@RequestMapping("/save")
	public byte[] save(HttpServletRequest request) {
		RequestUtils.setRequestParameterToAttribute(request);
		String actorId = RequestUtils.getActorId(request);
		String currentType = null;
		String code = request.getParameter("code");
		String expId = request.getParameter("expId");
		String currentStep = request.getParameter("currentStep");
		String previousType = request.getParameter("previousType");
		String nextType = request.getParameter("nextType");
		String previousObjectIds = request.getParameter("previousObjectIds");
		String nextObjectIds = request.getParameter("nextObjectIds");
		if (StringUtils.isNotEmpty(currentStep) && StringUtils.isNotEmpty(code)) {
			DataItemDefinition current = dataItemDefinitionService.getDataItemDefinitionByCode(code);
			if (current != null) {
				currentType = current.getType();
				int sort = 0;
				List<ExportPreprocessor> preprocessors = new ArrayList<ExportPreprocessor>();
				if (StringUtils.isNotEmpty(previousObjectIds)) {
					StringTokenizer token = new StringTokenizer(previousObjectIds, ",");
					while (token.hasMoreTokens()) {
						String str = token.nextToken();
						sort++;
						ExportPreprocessor preprocessor = new ExportPreprocessor();
						preprocessor.setExpId(expId);
						preprocessor.setPreviousStep(str);
						preprocessor.setPreviousType(previousType);
						preprocessor.setSort(sort);
						preprocessor.setCreateBy(actorId);
						preprocessors.add(preprocessor);
					}
				}

				if (StringUtils.isNotEmpty(nextObjectIds)) {
					sort = 0;
					StringTokenizer token = new StringTokenizer(nextObjectIds, ",");
					while (token.hasMoreTokens()) {
						String str = token.nextToken();
						sort++;
						ExportPreprocessor preprocessor = new ExportPreprocessor();
						preprocessor.setExpId(expId);
						preprocessor.setNextStep(str);
						preprocessor.setNextType(nextType);
						preprocessor.setSort(sort);
						preprocessor.setCreateBy(actorId);
						preprocessors.add(preprocessor);
					}
				}

				exportPreprocessorService.saveAll(expId, currentStep, currentType, preprocessors);
				return ResponseUtils.responseResult(true);
			}
		}

		return ResponseUtils.responseResult(false);
	}

	@javax.annotation.Resource
	public void setDataItemDefinitionService(DataItemDefinitionService dataItemDefinitionService) {
		this.dataItemDefinitionService = dataItemDefinitionService;
	}

	@javax.annotation.Resource
	public void setExportPreprocessorService(ExportPreprocessorService exportPreprocessorService) {
		this.exportPreprocessorService = exportPreprocessorService;
	}

}
