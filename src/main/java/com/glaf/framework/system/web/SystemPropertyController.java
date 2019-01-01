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

package com.glaf.framework.system.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.config.SystemProperties;
import com.glaf.core.util.FileUtils;
import com.glaf.core.util.RequestUtils;
import com.glaf.core.util.ResponseUtils;
import com.glaf.framework.system.config.SystemConfig;
import com.glaf.framework.system.domain.SystemProperty;
import com.glaf.framework.system.service.ISystemPropertyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller("/sys/property")
@RequestMapping("/sys/property")
public class SystemPropertyController {

	private static final Log logger = LogFactory.getLog(SystemPropertyController.class);

	private ISystemPropertyService systemPropertyService;

	@RequestMapping("/edit")
	public ModelAndView edit(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String category = request.getParameter("category");
		if (StringUtils.isNotEmpty(category)) {
			List<SystemProperty> rows = systemPropertyService.getSystemProperties(category);
			if (rows != null && !rows.isEmpty()) {
				for (SystemProperty p : rows) {
					if (StringUtils.equals(p.getInputType(), "combobox")) {
						if (StringUtils.isNotEmpty(p.getInitValue()) && StringUtils.startsWith(p.getInitValue(), "[")
								&& StringUtils.endsWith(p.getInitValue(), "]")) {
							try {
								JSONArray array = JSON.parseArray(p.getInitValue());
								p.setArray(array);
								StringBuilder buffer = new StringBuilder();
								for (int i = 0, len = array.size(); i < len; i++) {
									JSONObject json = array.getJSONObject(i);
									buffer.append("<option value=\"").append(json.getString("value")).append("\">")
											.append(json.getString("name")).append("</option>")
											.append(FileUtils.newline);
								}
								p.setSelectedScript(buffer.toString());
							} catch (Exception ex) {

								logger.error("parse json error :" + p.getInitValue(), ex);
							}
						}
					}
				}
			}
			request.setAttribute("rows", rows);
		}
		String jx_view = request.getParameter("jx_view");

		if (StringUtils.isNotEmpty(jx_view)) {
			return new ModelAndView(jx_view, modelMap);
		}

		return new ModelAndView("/sys/property/edit");
	}

	@RequestMapping("/properties.json")
	@ResponseBody
	public List<SystemProperty> properties(String category) {
		return systemPropertyService.getSystemProperties(category);
	}

	@RequestMapping("/property.json")
	@ResponseBody
	public SystemProperty property(String id) {
		return systemPropertyService.getSystemPropertyById(id);
	}

	@RequestMapping("/saveAll")
	public ModelAndView saveAll(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String category = request.getParameter("category");
		if (StringUtils.isNotEmpty(category)) {
			List<SystemProperty> rows = systemPropertyService.getSystemProperties(category);
			for (SystemProperty p : rows) {
				String key = p.getName();
				if (StringUtils.equals(key, "token")) {
					continue;
				}
				String value = request.getParameter(key);
				if (value != null) {
					p.setValue(value);
				}
			}

			SystemProperty prop = new SystemProperty();
			prop.setName("serviceUrl");
			prop.setTitle("服务地址");
			prop.setValue(RequestUtils.getServiceUrl(request));
			prop.setCategory("SYS");
			prop.setLocked(0);
			rows.add(prop);

			systemPropertyService.saveAll(rows);
		}

		String jx_view = request.getParameter("jx_view");

		if (StringUtils.isNotEmpty(jx_view)) {
			return new ModelAndView(jx_view, modelMap);
		}

		return this.edit(request, modelMap);
	}

	@ResponseBody
	@RequestMapping("/saveCfg")
	public byte[] saveCfg(HttpServletRequest request) {
		RequestUtils.setRequestParameterToAttribute(request);
		String category = request.getParameter("category");
		if (StringUtils.isNotEmpty(category)) {
			List<SystemProperty> rows = systemPropertyService.getSystemProperties(category);
			for (SystemProperty p : rows) {
				String key = p.getName();
				String value = request.getParameter(key);
				if (value != null) {
					p.setValue(value);
				}
			}

			SystemProperty prop = new SystemProperty();
			prop.setName("serviceUrl");
			prop.setTitle("服务地址");
			prop.setValue(RequestUtils.getServiceUrl(request));
			prop.setCategory("SYS");
			prop.setLocked(0);
			rows.add(prop);

			systemPropertyService.saveAll(rows);

			SystemProperties.reload();
			SystemConfig.reload();

			return ResponseUtils.responseJsonResult(true, "保存成功！");
		}
		return ResponseUtils.responseJsonResult(false);
	}

	@javax.annotation.Resource
	public void setSystemPropertyService(ISystemPropertyService systemPropertyService) {
		this.systemPropertyService = systemPropertyService;
	}

}