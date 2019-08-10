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

package com.glaf.template.web.springmvc;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.glaf.core.security.LoginContext;
import com.glaf.core.service.ITableDataService;
import com.glaf.core.util.FileUtils;
import com.glaf.core.util.Paging;
import com.glaf.core.util.ParamUtils;
import com.glaf.core.util.RequestUtils;
import com.glaf.core.util.ResponseUtils;
import com.glaf.core.util.Tools;
import com.glaf.template.Template;
import com.glaf.template.query.TemplateQuery;
import com.glaf.template.service.ITemplateService;

@Controller("/sys/template")
@RequestMapping("/sys/template")
public class SystemTemplateController {
	protected final static Log logger = LogFactory.getLog(SystemTemplateController.class);

	protected ITemplateService templateService;

	protected ITableDataService tableDataService;

	@RequestMapping("/delete")
	public ModelAndView deleteTemplate(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String templateId = request.getParameter("templateId");
		if (StringUtils.isNotEmpty(templateId)) {
			templateService.deleteTemplate(templateId);
		}
		return this.list(request, modelMap);
	}

	@RequestMapping("/download")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		RequestUtils.setRequestParameterToAttribute(request);
		String templateId = request.getParameter("templateId");
		if (StringUtils.isNotEmpty(templateId)) {
			Template template = templateService.getTemplate(templateId);
			if (template != null) {
				try {
					ResponseUtils.download(request, response, template.getData(), template.getDataFile());
				} catch (Exception e) {
				}
			}
		}
	}

	@RequestMapping("/edit")
	public ModelAndView edit(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String templateId = request.getParameter("templateId");
		if (StringUtils.isNotEmpty(templateId)) {
			Template template = templateService.getTemplate(templateId);
			modelMap.put("template", template);
		}
		String jx_view = request.getParameter("jx_view");

		if (StringUtils.isNotEmpty(jx_view)) {
			return new ModelAndView(jx_view, modelMap);
		}

		return new ModelAndView("/sys/template/edit");
	}

	public ITemplateService getTemplateService() {
		return templateService;
	}

	@RequestMapping("/json")
	@ResponseBody
	public byte[] json(HttpServletRequest request) throws IOException {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		TemplateQuery query = new TemplateQuery();
		Tools.populate(query, params);
		query.deleteFlag(0);
		query.setActorId(loginContext.getActorId());
		query.setLoginContext(loginContext);
		/**
		 * 此处业务逻辑需自行调整
		 */
		if (!loginContext.isSystemAdministrator()) {
			String actorId = loginContext.getActorId();
			query.createBy(actorId);
		}

		int start = 0;
		int limit = 10;
		String orderName = null;
		String order = null;

		int pageNo = ParamUtils.getInt(params, "page");
		limit = ParamUtils.getInt(params, "rows");
		start = (pageNo - 1) * limit;
		orderName = ParamUtils.getString(params, "sortName");
		order = ParamUtils.getString(params, "sortOrder");

		if (start < 0) {
			start = 0;
		}

		if (limit <= 0) {
			limit = Paging.DEFAULT_PAGE_SIZE;
		}

		JSONObject result = new JSONObject();
		int total = templateService.getTemplateCountByQueryCriteria(query);
		if (total > 0) {
			result.put("total", total);
			result.put("totalCount", total);
			result.put("totalRecords", total);
			result.put("start", start);
			result.put("startIndex", start);
			result.put("limit", limit);
			result.put("pageSize", limit);

			if (StringUtils.isNotEmpty(orderName)) {
				query.setSortOrder(orderName);
				if (StringUtils.equals(order, "desc")) {
					query.setSortOrder(" desc ");
				}
			}

			List<Template> list = templateService.getTemplatesByQueryCriteria(start, limit, query);

			if (list != null && !list.isEmpty()) {
				JSONArray rowsJSON = new JSONArray();

				result.put("rows", rowsJSON);

				for (Template template : list) {
					JSONObject rowJSON = template.toJsonObject();
					rowJSON.put("id", template.getTemplateId());
					rowJSON.put("templateId", template.getTemplateId());
					rowJSON.put("startIndex", ++start);
					rowsJSON.add(rowJSON);
				}

			}
		} else {
			JSONArray rowsJSON = new JSONArray();
			result.put("rows", rowsJSON);
			result.put("total", total);
		}
		return result.toJSONString().getBytes("UTF-8");
	}

	@RequestMapping
	public ModelAndView list(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		Map<String, Object> paramMap = RequestUtils.getParameterMap(request);

		String nodeCode = request.getParameter("nodeCode");
		if (StringUtils.isEmpty(nodeCode)) {
			nodeCode = "template_category";
		}
		modelMap.put("nodeCode", nodeCode);

		TemplateQuery query = new TemplateQuery();
		Tools.populate(query, paramMap);

		query.setPageSize(-1);
		query.setParameter(paramMap);
		List<Template> templates = templateService.getTemplates(query);
		modelMap.put("templates", templates);

		String jx_view = request.getParameter("jx_view");

		if (StringUtils.isNotEmpty(jx_view)) {
			return new ModelAndView(jx_view, modelMap);
		}

		return new ModelAndView("/sys/template/list", modelMap);
	}

	@RequestMapping("/main")
	public ModelAndView main(HttpServletRequest request, ModelMap modelMap) {
		// RequestUtils.setRequestParameterToAttribute(request);
		String jx_view = request.getParameter("jx_view");

		RequestUtils.setRequestParameterToAttribute(request);

		if (StringUtils.isNotEmpty(jx_view)) {
			return new ModelAndView(jx_view, modelMap);
		}

		return new ModelAndView("/sys/template/main");
	}

	@RequestMapping("/save")
	public ModelAndView save(HttpServletRequest request, ModelMap modelMap) throws IOException {
		LoginContext loginContext = RequestUtils.getLoginContext(request);

		String templateId = request.getParameter("templateId");
		Template template = null;
		if (StringUtils.isNotEmpty(templateId)) {
			template = templateService.getTemplate(templateId);
		}

		if (template == null) {
			template = new Template();
			template.setCreateBy(loginContext.getActorId());
		}

		// 将当前上下文初始化给 CommonsMutipartResolver（多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
			Map<String, Object> paramMap = RequestUtils.getParameterMap(req);
			Tools.populate(template, paramMap);

			String nodeId = ParamUtils.getString(paramMap, "nodeId");
			if (nodeId != null) {

			}

			Map<String, MultipartFile> fileMap = req.getFileMap();
			Set<Entry<String, MultipartFile>> entrySet = fileMap.entrySet();
			for (Entry<String, MultipartFile> entry : entrySet) {
				MultipartFile mFile = entry.getValue();
				String filename = mFile.getOriginalFilename();
				if (mFile.getSize() > 0) {
					template.setFileSize(mFile.getSize());
					int fileType = 0;
					if (filename.endsWith(".ftl")) {
						fileType = 52;
						template.setLanguage("freemarker");
						template.setContent(new String(mFile.getBytes()));
					} else if (filename.endsWith(".vm")) {
						fileType = 54;
						template.setLanguage("velocity");
						template.setContent(new String(mFile.getBytes()));
					} else if (filename.endsWith(".xml")) {
						fileType = 60;
						template.setContent(new String(mFile.getBytes()));
					} else if (filename.endsWith(".htm") || filename.endsWith(".html")) {
						fileType = 80;
						template.setContent(new String(mFile.getBytes()));
					} else if (filename.endsWith(".js")) {
						fileType = 82;
						template.setContent(new String(mFile.getBytes()));
					} else if (filename.endsWith(".css")) {
						fileType = 84;
						template.setContent(new String(mFile.getBytes()));
					} else if (filename.endsWith(".txt")) {
						fileType = 85;
						template.setContent(new String(mFile.getBytes()));
					}

					template.setDataFile(filename);
					template.setFileType(fileType);
					template.setCreateDate(new Date());
					template.setData(mFile.getBytes());
					template.setLastModified(System.currentTimeMillis());
					template.setTemplateType(FileUtils.getFileExt(filename));
					break;
				}
			}

			templateService.saveTemplate(template);
		}

		return this.list(request, modelMap);
	}

	@javax.annotation.Resource
	public void setTableDataService(ITableDataService tableDataService) {
		this.tableDataService = tableDataService;
	}

	@javax.annotation.Resource
	public void setTemplateService(ITemplateService templateService) {
		this.templateService = templateService;
	}

	@RequestMapping("/view")
	public ModelAndView view(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String templateId = request.getParameter("templateId");
		if (StringUtils.isNotEmpty(templateId)) {
			Template template = templateService.getTemplate(templateId);
			modelMap.put("template", template);
		}
		String jx_view = request.getParameter("jx_view");

		if (StringUtils.isNotEmpty(jx_view)) {
			return new ModelAndView(jx_view, modelMap);
		}

		return new ModelAndView("/sys/template/view");
	}

}