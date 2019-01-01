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

package com.glaf.framework.dao;

import com.glaf.core.config.BaseConfiguration;
import com.glaf.core.config.Configuration;
import com.glaf.core.dao.EntityDAO;
import com.glaf.core.id.Dbid;
import com.glaf.core.id.IdBlock;
import com.glaf.core.util.ClassUtils;
import com.glaf.core.util.DateUtils;
import com.glaf.core.util.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("entityDAO")
public class MyBatisEntityDAOImpl implements EntityDAO {
	protected final static Log logger = LogFactory.getLog(MyBatisEntityDAOImpl.class);

	private static final Configuration conf = BaseConfiguration.create();

	private SqlSessionTemplate sqlSessionTemplate;

	@SuppressWarnings("unchecked")
	public void delete(String statementId, Object parameterObject) {
		if (parameterObject instanceof Map) {
			Map<String, Object> dataMap = (Map<String, Object>) parameterObject;
			String className = (String) dataMap.get("className");
			if (className != null) {
				Object object = ClassUtils.instantiateObject(className);
				Tools.populate(object, dataMap);
				getSqlSession().delete(statementId, object);
			} else {
				getSqlSession().delete(statementId, dataMap);
			}
		} else {
			getSqlSession().delete(statementId, parameterObject);
		}
	}

	public void deleteAll(String statementId, List<Object> rows) {
		for (Object row : rows) {
			getSqlSession().delete(statementId, row);
		}
	}

	public void deleteById(String statementId, Object rowId) {
		getSqlSession().delete(statementId, rowId);
	}

	public Object getById(String statementId, Object parameterObject) {
		return getSqlSession().selectOne(statementId, parameterObject);
	}

	public int getCount(String statementId, Object parameterObject) {
		int totalCount;
		SqlSession session = getSqlSession();

		Object object;
		if (parameterObject != null) {
			object = session.selectOne(statementId, parameterObject);
		} else {
			object = session.selectOne(statementId);
		}

		if (object instanceof Integer) {
			totalCount = (int) object;
		} else if (object instanceof Long) {
			Long iCount = (Long) object;
			totalCount = iCount.intValue();
		} else if (object instanceof BigDecimal) {
			BigDecimal bg = (BigDecimal) object;
			totalCount = bg.intValue();
		} else if (object instanceof BigInteger) {
			BigInteger bi = (BigInteger) object;
			totalCount = bi.intValue();
		} else {
			String value = object.toString();
			totalCount = Integer.parseInt(value);
		}
		return totalCount;
	}

	public List<Object> getList(String statementId, Object parameterObject) {
		// logger.debug("statementId:" + statementId);
		// logger.debug("parameter:" + parameterObject);
		return getSqlSession().selectList(statementId, parameterObject);
	}

	/**
	 * 获取指定表的主键列的最大值
	 * 
	 * @return
	 */
	public synchronized long getMaxId(String tablename, String idColumn) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tablename", tablename);
		params.put("idColumn", idColumn);
		Long oldValue = getSqlSession().selectOne("getMaxId", params);
		if (oldValue == null) {
			oldValue = 0L;
		}
		return oldValue;
	}

	public Object getSingleObject(String statementId, Object parameterObject) {
		return getSqlSession().selectOne(statementId, parameterObject);
	}

	private SqlSessionTemplate getSqlSession() {
		return sqlSessionTemplate;
	}

	/**
	 * 获取下一条记录编号
	 * 
	 * @return
	 */
	public synchronized int getTableUserMaxId(String tablename, String idColumn, String createBy) {
		int day = DateUtils.getNowYearMonthDay();
		String idLike = day + "/" + createBy + "-%";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tablename", tablename);
		params.put("idColumn", idColumn);
		params.put("idLike", idLike);

		String str = getSqlSession().selectOne("getTableUserMaxId", params);
		if (StringUtils.isNotEmpty(str) && StringUtils.contains(str, "-")) {
			str = str.substring(str.lastIndexOf("-") + 1);
			str = str.trim();
			if (StringUtils.isNumeric(str)) {
				return Integer.parseInt(str) + 1;
			}
		}

		return 1;
	}

	public void insert(String statementId, Object parameterObject) {
		getSqlSession().insert(statementId, parameterObject);
	}

	public void insertAll(String statementId, List<Object> rows) {
		for (Object entity : rows) {
			getSqlSession().insert(statementId, entity);
		}
	}

	/**
	 * 获取下一条记录编号
	 * 
	 * @return
	 */
	public IdBlock nextDbidBlock() {
		Dbid dbid = getSqlSession().selectOne("getNextDbId", "next.dbid");
		if (dbid == null) {
			dbid = new Dbid();
			dbid.setTitle("系统内置主键");
			dbid.setName("next.dbid");
			dbid.setValue("10001");
			dbid.setVersion(1);
			getSqlSession().insert("inertNextDbId", dbid);
			dbid = getSqlSession().selectOne("getNextDbId", "next.dbid");
		}
		long oldValue = Long.parseLong(dbid.getValue());
		long newValue = oldValue + conf.getInt("dbid_step", 1000);
		dbid.setName("next.dbid");
		dbid.setTitle("系统内置主键");
		dbid.setValue(Long.toString(newValue + 1));
		dbid.setVersion(dbid.getVersion() + 1);
		getSqlSession().update("updateNextDbId", dbid);
		return new IdBlock(oldValue, newValue - 1);
	}

	/**
	 * 获取下一条记录编号
	 * 
	 * @return
	 */
	public synchronized IdBlock nextDbidBlock(String name) {
		Dbid dbid = getSqlSession().selectOne("getNextDbId", name);
		if (dbid == null) {
			dbid = new Dbid();
			dbid.setTitle("系统内置主键");
			dbid.setName(name);
			dbid.setValue("1");
			dbid.setVersion(1);
			getSqlSession().insert("inertNextDbId", dbid);
			dbid = getSqlSession().selectOne("getNextDbId", name);
		}
		int setup = 100;
		if (StringUtils.equalsIgnoreCase(name, "useradd") || StringUtils.equalsIgnoreCase(name, "sys_tenant")
				|| StringUtils.equalsIgnoreCase(name, "sys_organization") || StringUtils.startsWith(name, "tenant_")) {
			setup = 1;
		} else if (StringUtils.equalsIgnoreCase(name, "HEALTH_DIETARY_ITEM")) {
			setup = 500;
		} else if (StringUtils.equalsIgnoreCase(name, "SYS_SCHEDULER_EXECUTION")) {
			setup = 500;
		}
		long oldValue = Long.parseLong(dbid.getValue());
		long newValue = oldValue + conf.getInt("dbid_step_" + name, setup);
		dbid.setName(name);
		dbid.setTitle("系统内置主键");
		dbid.setValue(Long.toString(newValue + 1));
		dbid.setVersion(dbid.getVersion() + 1);
		getSqlSession().update("updateNextDbId", dbid);
		if (newValue - oldValue > 1) {
			return new IdBlock(oldValue, newValue - 1);
		}
		return new IdBlock(oldValue, newValue);
	}

	public void select(String statement, Object parameter, ResultHandler<?> handler) {
		getSqlSession().select(statement, parameter, handler);
	}

	public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler<?> handler) {
		getSqlSession().select(statement, parameter, rowBounds, handler);
	}

	public void setConnection(Connection con) {

	}

	@javax.annotation.Resource
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void update(String statementId, Object parameterObject) {
		getSqlSession().update(statementId, parameterObject);
	}

	public void updateAll(String statementId, List<Object> rows) {
		for (Object entity : rows) {
			getSqlSession().update(statementId, entity);
		}
	}

}