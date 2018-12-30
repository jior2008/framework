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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.security.LoginContext;
import com.glaf.core.security.SecurityUtils;
import com.glaf.core.util.Paging;
import com.glaf.core.util.ParamUtils;
import com.glaf.core.util.RequestUtils;
import com.glaf.core.util.ResponseUtils;
import com.glaf.core.util.StringTools;
import com.glaf.core.util.Tools;
import com.glaf.framework.system.config.DBConfiguration;
import com.glaf.framework.system.config.JdbcConnectionFactory;
import com.glaf.framework.system.config.DatabaseConnectionConfig;
import com.glaf.framework.system.domain.Database;
import com.glaf.framework.system.factory.DatabaseFactory;
import com.glaf.framework.system.query.DatabaseQuery;
import com.glaf.framework.system.security.RSAUtils;
import com.glaf.framework.system.service.IDatabaseService;

/**
 * 
 * SpringMVC控制器
 * 
 */

@Controller("/sys/database")
@RequestMapping("/sys/database")
public class DatabaseController {
	protected static final Log logger = LogFactory.getLog(DatabaseController.class);

	protected static AtomicBoolean running = new AtomicBoolean(false);

	protected IDatabaseService databaseService;

	public DatabaseController() {

	}

	@RequestMapping("/chooseDatabases")
	public ModelAndView chooseDatabases(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String selectedDatabaseIds = request.getParameter("selectedDatabaseIds");
		DatabaseConnectionConfig config = new DatabaseConnectionConfig();
		List<Database> databases = config.getDatabases();
		if (databases != null && !databases.isEmpty()) {
			List<Long> selected = new ArrayList<Long>();
			List<Database> unselected = new ArrayList<Database>();
			if (selectedDatabaseIds != null && StringUtils.isNotEmpty(selectedDatabaseIds)) {
				List<Long> ids = StringTools.splitToLong(selectedDatabaseIds);
				for (Database database : databases) {
					if (ids.contains(database.getId())) {
						selected.add(database.getId());
					} else {
						unselected.add(database);
					}
				}
				request.setAttribute("selected", selected);
				request.setAttribute("unselected", databases);
			} else {
				request.setAttribute("selected", selected);
				request.setAttribute("unselected", databases);
			}
			request.setAttribute("databases", databases);

			StringBuffer bufferx = new StringBuffer();
			StringBuffer buffery = new StringBuffer();

			if (databases != null && databases.size() > 0) {
				for (int j = 0; j < databases.size(); j++) {
					Database d = (Database) databases.get(j);
					if (selected != null && selected.contains(d.getId())) {
						buffery.append("\n<option value=\"").append(d.getId()).append("\">").append(d.getTitle())
								.append(" [").append(d.getMapping()).append("]").append("</option>");
					} else {
						bufferx.append("\n<option value=\"").append(d.getId()).append("\">").append(d.getTitle())
								.append(" [").append(d.getMapping()).append("]").append("</option>");
					}
				}
			}
			request.setAttribute("bufferx", bufferx.toString());
			request.setAttribute("buffery", buffery.toString());
		}

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		return new ModelAndView("/sys/database/chooseDatabases", modelMap);
	}

	@RequestMapping("/delete")
	public byte[] deleteById(HttpServletRequest request) throws IOException {
		long databaseId = RequestUtils.getLong(request, "id");
		if (databaseId > 0) {
			try {
				databaseService.deleteById(databaseId);
				return ResponseUtils.responseJsonResult(true);
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
		return ResponseUtils.responseJsonResult(false);
	}

	@RequestMapping("/edit")
	public ModelAndView edit(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);

		Database database = databaseService.getDatabaseById(RequestUtils.getLong(request, "id"));
		if (database != null) {
			request.setAttribute("database", database);
			request.setAttribute("databaseId_enc", RSAUtils.encryptString(String.valueOf(database.getId())));
			request.setAttribute("databaseName_enc", RSAUtils.encryptString(database.getName()));
			request.setAttribute("parentId", database.getParentId());
		} else {
			request.setAttribute("parentId", RequestUtils.getLong(request, "parentId"));
		}

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		return new ModelAndView("/sys/database/edit", modelMap);
	}

	@ResponseBody
	@RequestMapping("/initDB")
	public byte[] initDB(HttpServletRequest request) {
		if (running.get()) {
			return ResponseUtils.responseJsonResult(false, "不能执行初始化，已经有任务在执行中，请等待其他任务完成再执行。");
		}

		try {
			running.set(true);
			Database db = null;
			if (StringUtils.isNotEmpty(request.getParameter("id"))) {
				db = databaseService.getDatabaseById(RequestUtils.getLong(request, "id"));
				if (db != null) {
					String name = db.getName();
					String dbType = db.getType();
					String host = db.getHost();

					int port = db.getPort();
					String databaseName = db.getDbname();
					String user = db.getUser();
					String password = SecurityUtils.decode(db.getKey(), db.getPassword());

					DBConfiguration.addDataSourceProperties(name, dbType, host, port, databaseName, user, password);
					if (JdbcConnectionFactory.checkConnection(name)) {
						db.setVerify("Y");
						db.setInitFlag("Y");
						databaseService.update(db);

						return ResponseUtils.responseJsonResult(true, "数据库已经成功初始化。");
					}
				}
			}

		} catch (Exception ex) {

			logger.error(ex);
		} finally {
			running.set(false);
		}
		return ResponseUtils.responseJsonResult(false, "服务器配置错误。");
	}

	@RequestMapping("/json")
	@ResponseBody
	public byte[] json(HttpServletRequest request, ModelMap modelMap) throws IOException {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		DatabaseQuery query = new DatabaseQuery();
		Tools.populate(query, params);
		query.deleteFlag(0);
		query.setActorId(loginContext.getActorId());
		query.setLoginContext(loginContext);

		if (!loginContext.isSystemAdministrator()) {
			String actorId = loginContext.getActorId();
			query.createBy(actorId);
		}

		String keywordsLike_base64 = request.getParameter("keywordsLike_base64");
		if (StringUtils.isNotEmpty(keywordsLike_base64)) {
			String keywordsLike = new String(Base64.decodeBase64(keywordsLike_base64));
			query.setKeywordsLike(keywordsLike);
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
		int total = databaseService.getDatabaseCountByQueryCriteria(query);
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

			List<Database> list = databaseService.getDatabasesByQueryCriteria(start, limit, query);

			if (list != null && !list.isEmpty()) {
				JSONArray rowsJSON = new JSONArray();

				result.put("rows", rowsJSON);

				for (Database database : list) {
					JSONObject rowJSON = database.toJsonObject();
					rowJSON.put("id", database.getId());
					rowJSON.put("rowId", database.getId());
					rowJSON.put("databaseId", database.getId());
					rowJSON.put("startIndex", ++start);
					rowJSON.remove("key");
					rowJSON.remove("user");
					rowJSON.remove("password");
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

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		return new ModelAndView("/sys/database/list", modelMap);
	}

	@RequestMapping("/query")
	public ModelAndView query(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		return new ModelAndView("/sys/database/query", modelMap);
	}

	@ResponseBody
	@RequestMapping(value = "/reloadDB", method = RequestMethod.POST)
	public byte[] reloadDB(HttpServletRequest request) {
		try {
			DatabaseQuery query = new DatabaseQuery();
			query.active("1");
			List<Database> databases = databaseService.list(query);
			if (databases != null && !databases.isEmpty()) {
				DatabaseConnectionConfig cfg = new DatabaseConnectionConfig();
				for (Database database : databases) {
					if (StringUtils.equalsIgnoreCase(database.getType(), "mysql")
							|| StringUtils.equalsIgnoreCase(database.getType(), "sqlserver")
							|| StringUtils.equalsIgnoreCase(database.getType(), "postgresql")
							|| StringUtils.equalsIgnoreCase(database.getType(), "oracle")) {
						cfg.checkConfig(database);
					}
				}
			}
			DatabaseFactory.clearAll();
			return ResponseUtils.responseResult(true);
		} catch (Exception ex) {
			logger.error(ex);
		}

		return ResponseUtils.responseResult(false);
	}

	@ResponseBody
	@RequestMapping("/saveAccessor")
	public byte[] saveAccessor(HttpServletRequest request) {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		long databaseId = RequestUtils.getLong(request, "databaseId");
		String actorId = request.getParameter("actorId");
		String operation = request.getParameter("operation");
		if (databaseId > 0 && actorId != null) {
			/**
			 * 保证添加的部门是分级管理员管辖的部门
			 */
			if (loginContext.isSystemAdministrator()) {
				if (StringUtils.equals(operation, "revoke")) {
					databaseService.deleteAccessor(databaseId, actorId);
				} else {
					databaseService.createAccessor(databaseId, actorId);
				}
				return ResponseUtils.responseResult(true);
			}
		}
		return ResponseUtils.responseJsonResult(false);
	}

	@ResponseBody
	@RequestMapping("/saveDB")
	public byte[] saveDB(HttpServletRequest request) {
		Database database = null;
		if (StringUtils.isNotEmpty(request.getParameter("id"))) {
			database = databaseService.getDatabaseById(RequestUtils.getLong(request, "id"));
		}
		if (database == null) {
			database = new Database();
		}

		String user = request.getParameter("user");
		String password = request.getParameter("password");
		database.setUser(user);
		database.setPassword(password);
		database.setTitle(request.getParameter("title"));
		database.setParentId(RequestUtils.getLong(request, "parentId"));
		database.setDiscriminator(request.getParameter("discriminator"));
		database.setHost(request.getParameter("host"));
		database.setPort(RequestUtils.getInt(request, "port"));
		database.setMapping(request.getParameter("mapping"));
		database.setSection(request.getParameter("section"));
		database.setType(request.getParameter("type"));
		database.setRunType(request.getParameter("runType"));
		database.setUseType(request.getParameter("useType"));
		database.setLevel(RequestUtils.getInt(request, "level"));
		database.setPriority(RequestUtils.getInt(request, "priority"));
		database.setOperation(RequestUtils.getInt(request, "operation"));
		database.setDbname(request.getParameter("dbname"));
		database.setBucket(request.getParameter("bucket"));
		database.setCatalog(request.getParameter("catalog"));
		database.setInfoServer(request.getParameter("infoServer"));
		database.setLoginAs(request.getParameter("loginAs"));
		database.setLoginUrl(request.getParameter("loginUrl"));
		database.setTicket(request.getParameter("ticket"));
		database.setProgramId(request.getParameter("programId"));
		database.setProgramName(request.getParameter("programName"));
		database.setUserNameKey(request.getParameter("userNameKey"));
		database.setProviderClass(request.getParameter("providerClass"));
		database.setRemoteUrl(request.getParameter("remoteUrl"));
		database.setServerId(RequestUtils.getLong(request, "serverId"));
		database.setSysId(request.getParameter("sysId"));
		database.setActive(request.getParameter("active"));
		database.setIntToken(RequestUtils.getInt(request, "intToken"));
		database.setSort(RequestUtils.getInt(request, "sort"));

		if (StringUtils.isEmpty(database.getType())) {
			database.setType("sqlserver");
		}

		try {
			this.databaseService.save(database);
			return ResponseUtils.responseJsonResult(true);
		} catch (Exception ex) {

			logger.error(ex);
		}
		return ResponseUtils.responseJsonResult(false);
	}

	@javax.annotation.Resource
	public void setDatabaseService(IDatabaseService databaseService) {
		this.databaseService = databaseService;
	}

	/**
	 * 显示排序页面
	 * 
	 * @param request
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/showSort")
	public ModelAndView showSort(HttpServletRequest request, ModelMap modelMap) {
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		DatabaseQuery query = new DatabaseQuery();
		Tools.populate(query, params);
		query.deleteFlag(0);

		List<Database> list = databaseService.list(query);
		request.setAttribute("list", list);

		return new ModelAndView("/sys/database/showSort", modelMap);
	}

	@ResponseBody
	@RequestMapping("/verify")
	public byte[] verify(HttpServletRequest request) {
		try {
			Database database = null;
			if (StringUtils.isNotEmpty(request.getParameter("id"))) {
				database = databaseService.getDatabaseById(RequestUtils.getLong(request, "id"));
			}
			if (database == null) {
				database = new Database();
			}

			String user = request.getParameter("user");
			String password = request.getParameter("password");

			if (!"88888888".equals(password)) {
				String key = SecurityUtils.genKey();
				String pass = SecurityUtils.encode(key, password);
				database.setKey(key);
				database.setPassword(pass);
			}

			database.setUser(user);
			database.setTitle(request.getParameter("title"));
			database.setParentId(RequestUtils.getLong(request, "parentId"));
			database.setDiscriminator(request.getParameter("discriminator"));
			database.setHost(request.getParameter("host"));
			database.setPort(RequestUtils.getInt(request, "port"));
			database.setMapping(request.getParameter("mapping"));
			database.setType(request.getParameter("type"));
			database.setRunType(request.getParameter("runType"));
			database.setLevel(RequestUtils.getInt(request, "level"));
			database.setPriority(RequestUtils.getInt(request, "priority"));
			database.setOperation(RequestUtils.getInt(request, "operation"));
			database.setDbname(request.getParameter("dbname"));
			database.setBucket(request.getParameter("bucket"));
			database.setCatalog(request.getParameter("catalog"));
			database.setInfoServer(request.getParameter("infoServer"));
			database.setLoginAs(request.getParameter("loginAs"));
			database.setLoginUrl(request.getParameter("loginUrl"));
			database.setTicket(request.getParameter("ticket"));
			database.setProgramId(request.getParameter("programId"));
			database.setProgramName(request.getParameter("programName"));
			database.setUserNameKey(request.getParameter("userNameKey"));
			database.setProviderClass(request.getParameter("providerClass"));
			database.setRemoteUrl(request.getParameter("remoteUrl"));
			database.setActive(request.getParameter("active"));
			database.setIntToken(RequestUtils.getInt(request, "intToken"));
			database.setSort(RequestUtils.getInt(request, "sort"));
			database.setServerId(RequestUtils.getLong(request, "serverId"));
			database.setSysId(request.getParameter("sysId"));

			String name = database.getName();
			String dbType = database.getType();
			String host = database.getHost();
			int port = database.getPort();
			String databaseName = database.getDbname();
			if ("88888888".equals(password)) {
				password = SecurityUtils.decode(database.getKey(), database.getPassword());
			}

			DatabaseConnectionConfig config = new DatabaseConnectionConfig();
			if (config.checkConnectionImmediately(database)) {
				DBConfiguration.addDataSourceProperties(name, dbType, host, port, databaseName, user, password);
				database.setVerify("Y");
				databaseService.update(database);
				return ResponseUtils.responseJsonResult(true, "数据库配置正确。");
			}

		} catch (Exception ex) {

			logger.error(ex);
		}
		return ResponseUtils.responseJsonResult(false, "服务器配置错误。");
	}

	@ResponseBody
	@RequestMapping("/verify2")
	public byte[] verify2(HttpServletRequest request) {
		try {
			Database database = null;
			if (StringUtils.isNotEmpty(request.getParameter("id"))) {
				database = databaseService.getDatabaseById(RequestUtils.getLong(request, "id"));
				if (database != null) {

					String name = database.getName();

					if (JdbcConnectionFactory.checkConnection(name)) {
						database.setVerify("Y");
						databaseService.verify(database);
						return ResponseUtils.responseJsonResult(true, "数据库配置正确。");
					}

					String dbType = database.getType();
					String host = database.getHost();
					int port = database.getPort();
					String databaseName = database.getDbname();
					String user = database.getUser();
					String password = SecurityUtils.decode(database.getKey(), database.getPassword());
					// logger.debug("->password:" + password);

					DatabaseConnectionConfig config = new DatabaseConnectionConfig();
					if (config.checkConnectionImmediately(database)) {
						DBConfiguration.addDataSourceProperties(name, dbType, host, port, databaseName, user, password);
						logger.debug("->systemName:" + name);
						database.setVerify("Y");
						databaseService.verify(database);
						return ResponseUtils.responseJsonResult(true, "数据库配置正确。");
					}
				}
			}
		} catch (Exception ex) {

			logger.error(ex);
		}
		return ResponseUtils.responseJsonResult(false, "服务器配置错误。");
	}

	@RequestMapping("/verifyAll")
	public ModelAndView verifyAll(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);

		DatabaseQuery query = new DatabaseQuery();
		query.active("1");
		List<Database> databases = databaseService.list(query);
		request.setAttribute("databases", databases);

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view);
		}

		return new ModelAndView("/sys/database/verifyAll");
	}

	@RequestMapping("/view")
	public ModelAndView view(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		Database database = databaseService.getDatabaseById(RequestUtils.getLong(request, "id"));
		request.setAttribute("database", database);

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view);
		}

		return new ModelAndView("/sys/database/view");
	}

}
