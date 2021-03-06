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

package com.glaf.framework.system.config;

import com.alibaba.fastjson.JSONObject;
import com.glaf.core.base.ConnectionDefinition;
import com.glaf.core.config.Environment;
import com.glaf.core.config.SystemProperties;
import com.glaf.core.dialect.*;
import com.glaf.core.el.ExpressionTools;
import com.glaf.core.factory.ConnectionDefinitionJsonFactory;
import com.glaf.core.util.Constants;
import com.glaf.core.util.FileUtils;
import com.glaf.core.util.Hex;
import com.glaf.core.util.PropertiesUtils;
import com.glaf.framework.system.jdbc.connection.ConnectionConstants;
import com.glaf.framework.system.jdbc.datasource.MultiRoutingDataSource;
import com.glaf.framework.system.security.AESUtils;
import com.glaf.framework.system.security.RSAUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DBConfiguration {
	private static final Log logger = LogFactory.getLog(DBConfiguration.class);

	public static final String DIALECT = "dialect";

	public static final String JDBC_PROVIDER = "spring.datasource.provider";

	public static final String JDBC_AUTOCOMMIT = "spring.datasource.autocommit";

	private static final String JDBC_TYPE = "spring.datasource.type";

	public static final String JDBC_NAME = "spring.datasource.name";

	public static final String JDBC_PREFIX = "spring.datasource";

	public static final String JDBC_URL = "spring.datasource.url";

	public static final String JDBC_USER = "spring.datasource.username";

	public static final String JDBC_DRIVER = "spring.datasource.driverClassName";

	public static final String JDBC_PASSWORD = "spring.datasource.password";

	public static final String JDBC_ISOLATION = "spring.datasource.isolation";

	/**
	 * 连接池类型，支持druid,C3P0,DBCP,默认是druid
	 */
	public static final String JDBC_POOL_TYPE = "spring.datasource.pool_type";

	private static final String HOST = "host";

	public static final String PORT = "port";

	private static final String DATABASE = "databaseName";

	public static final String DATABASE_NAME = "databaseName";

	private static final String SUBJECT = "subject";

	private static final AtomicBoolean loading = new AtomicBoolean(false);

	private static final ConcurrentMap<String, ConnectionDefinition> dataSourceProperties = new ConcurrentHashMap<String, ConnectionDefinition>();

	private static final ConcurrentMap<String, Properties> jdbcTemplateProperties = new ConcurrentHashMap<String, Properties>();

	private static final ConcurrentMap<String, String> jsonProperties = new ConcurrentHashMap<String, String>();

	private static final ConcurrentMap<Integer, String> ISOLATION_LEVELS = new ConcurrentHashMap<Integer, String>();

	private static final ConcurrentMap<String, String> dbTypes = new ConcurrentHashMap<String, String>();

	static {
		ISOLATION_LEVELS.put(Connection.TRANSACTION_NONE, "NONE");
		ISOLATION_LEVELS.put(Connection.TRANSACTION_READ_UNCOMMITTED, "READ_UNCOMMITTED");
		ISOLATION_LEVELS.put(Connection.TRANSACTION_READ_COMMITTED, "READ_COMMITTED");
		ISOLATION_LEVELS.put(Connection.TRANSACTION_REPEATABLE_READ, "REPEATABLE_READ");
		ISOLATION_LEVELS.put(Connection.TRANSACTION_SERIALIZABLE, "SERIALIZABLE");
		init();
		reloadDS();
	}

	/**
	 * 添加数据源
	 * 
	 * @param name
	 *            名称
	 * @param props
	 *            属性
	 */
	public static void addDataSourceProperties(String name, Properties props) {
		if (!dataSourceProperties.containsKey(name)) {
			try {
				if (JdbcConnectionFactory.checkConnection(props)) {
					String url = props.getProperty(DBConfiguration.JDBC_URL);
					ConnectionDefinition conn = toConnectionDefinition(props);
					String dbType = getDatabaseType(url);
					conn.setUrl(url);
					conn.setType(dbType);
					dbTypes.put(name, dbType);
					dataSourceProperties.put(name, conn);
				}
			} catch (Exception ignored) {

			}
		}
	}

	/**
	 * 添加数据源
	 * 
	 * @param name
	 *            名称
	 * @param dbType
	 *            数据库类型
	 * @param host
	 *            主机
	 * @param port
	 *            端口
	 * @param databaseName
	 *            数据库名称
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 */
	public static void addDataSourceProperties(String name, String dbType, String host, int port, String databaseName,
			String user, String password) {
		Properties props = getTemplateProperties(dbType);
		if (props != null && !props.isEmpty()) {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("host", host);
			if (port > 0) {
				context.put("port", port);
			} else {
				context.put("port", props.getProperty(PORT));
			}
			context.put("databaseName", databaseName);
			String driver = props.getProperty(JDBC_DRIVER);
			String url = props.getProperty(JDBC_URL);
			url = com.glaf.core.el.ExpressionTools.evaluate(url, context);
			logger.debug("name:" + name);
			logger.debug("driver:" + driver);
			logger.debug("url:" + url);
			addDataSourceProperties(name, driver, url, user, password);
		}
	}

	/**
	 * 添加数据源
	 * 
	 * @param name
	 *            名称
	 * @param driver
	 *            驱动
	 * @param url
	 *            JDBC完整URL
	 * @param user
	 *            数据库用户名
	 * @param password
	 *            密码
	 */
	private static void addDataSourceProperties(String name, String driver, String url, String user, String password) {
		if (!dataSourceProperties.containsKey(name)) {
			Properties props = new Properties();
			props.put(JDBC_NAME, name);
			props.put(JDBC_DRIVER, driver);
			props.put(JDBC_URL, url);
			if (user != null) {
				props.put(JDBC_USER, user);
			}
			if (password != null) {
				props.put(JDBC_PASSWORD, password);
			}

			String dbType = getDatabaseType(url);
			if (StringUtils.equals(dbType, "postgresql")) {
				props.put(ConnectionConstants.PROP_VALIDATIONQUERY, " SELECT 1 ");
			} else if (StringUtils.equals(dbType, "sqlserver")) {
				props.put(ConnectionConstants.PROP_VALIDATIONQUERY, " SELECT 1 ");
			} else if (StringUtils.equals(dbType, "mysql")) {
				props.put(ConnectionConstants.PROP_VALIDATIONQUERY, " SELECT 1 ");
			} else if (StringUtils.equals(dbType, "hbase")) {
				props.put(ConnectionConstants.PROP_VALIDATIONQUERY, " SELECT 1 ");
			} else if (StringUtils.equals(dbType, "oracle")) {
				props.put(ConnectionConstants.PROP_VALIDATIONQUERY, " SELECT 1 FROM dual ");
			}

			try {
				if (JdbcConnectionFactory.checkConnection(props)) {
					MultiRoutingDataSource.addDataSource(name, props);
					ConnectionDefinition conn = toConnectionDefinition(props);
					conn.setUrl(url);
					conn.setType(dbType);
					dbTypes.put(name, dbType);
					props.put(JDBC_TYPE, dbType);
					dataSourceProperties.put(name, conn);
				}
			} catch (Exception ignored) {

			}
		}
	}

	private static String encodeJson(Properties props) {
		JSONObject jsonObject = new JSONObject();
		Enumeration<?> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			jsonObject.put(key, value);
		}
		String content = jsonObject.toJSONString();
		content = RSAUtils.encryptString(content);
		return content;
	}

	public static String encodeJsonCurrentSystem() {
		String systemName = Environment.getCurrentSystemName();
		if (jsonProperties.containsKey(systemName)) {
			return jsonProperties.get(systemName);
		}
		Properties props = getDataSourcePropertiesByName(systemName);
		if (props != null) {
			String content = encodeJson(props);
			jsonProperties.put(systemName, content);
		}
		return jsonProperties.get(systemName);
	}

	private static ConnectionDefinition getConnectionDefinition(String systemName) {
		if (systemName == null) {
			return null;
		}
		ConnectionDefinition model = dataSourceProperties.get(systemName);
		if (model != null) {
			try {
				JSONObject jsonObject = model.toJsonObject();
				return ConnectionDefinitionJsonFactory.jsonToObject(jsonObject);
			} catch (Exception ex) {
				// Ignore Exception
			}
		}
		return null;
	}

	public static List<ConnectionDefinition> getConnectionDefinitions() {
		List<ConnectionDefinition> rows = new java.util.ArrayList<ConnectionDefinition>();
		for (Entry<String, ConnectionDefinition> entry : dataSourceProperties.entrySet()) {
			String name = entry.getKey();
			ConnectionDefinition model = getConnectionDefinition(name);
			if (model != null && model.getName() != null) {
				rows.add(model);
			}
		}
		return rows;
	}

	private static String getCurrentDatabaseType() {
		String currentSystemName = Environment.getCurrentSystemName();
		ConnectionDefinition conn = getConnectionDefinition(currentSystemName);
		if (conn != null && conn.getType() != null) {
			return conn.getType();
		}
		return null;
	}

	public static Properties getCurrentDataSourceProperties() {
		String currentSystemName = Environment.getCurrentSystemName();
		ConnectionDefinition conn = getConnectionDefinition(currentSystemName);
        return toProperties(conn);
	}

	public static Dialect getCurrentDialect() {
		Map<String, Dialect> dialects = getDialects();
		String currentSystemName = Environment.getCurrentSystemName();
		ConnectionDefinition conn = getConnectionDefinition(currentSystemName);
		if (conn != null && conn.getType() != null) {
			String dbType = conn.getType();
			if (dbType == null) {
				dbType = getCurrentDatabaseType();
			}
			logger.debug("databaseType:" + dbType);
			return dialects.get(dbType);
		}
		return null;
	}

	public static String getCurrentDialectClass() {
		Properties dialects = getDialectMappings();
		String currentSystemName = Environment.getCurrentSystemName();
		ConnectionDefinition conn = getConnectionDefinition(currentSystemName);
		if (conn != null && conn.getType() != null) {
			String dbType = conn.getType();
			if (dbType == null) {
				dbType = getCurrentDatabaseType();
			}
			logger.debug("databaseType:" + dbType);
			assert dbType != null;
			return dialects.getProperty(dbType);
		}
		return null;
	}

	public static String getCurrentHibernateDialect() {
		Properties dialects = getHibernateDialectMappings();
		String currentSystemName = Environment.getCurrentSystemName();
		ConnectionDefinition conn = getConnectionDefinition(currentSystemName);
		if (conn != null && conn.getType() != null) {
			String dbType = conn.getType();
			if (dbType == null) {
				dbType = getCurrentDatabaseType();
			}
			logger.debug("databaseType:" + dbType);
			assert (dbType != null ? dbType : null) != null;
			return dialects.getProperty(dbType != null ? dbType : null);
		}
		return null;
	}

	public static Dialect getDatabaseDialect(Connection connection) {
		Dialect dialect = null;
		String dbType = JdbcConnectionFactory.getDatabaseType(connection);
		if (dbType != null) {
			dialect = getDialects().get(dbType);
		}
		return dialect;
	}

	public static Map<String, Dialect> getDatabaseDialects() {
		Map<String, Dialect> dialects = new java.util.HashMap<String, Dialect>();
		logger.debug("dataSourceProperties:" + dataSourceProperties);
		if (!dataSourceProperties.isEmpty()) {
			for (Entry<String, ConnectionDefinition> entry : dataSourceProperties.entrySet()) {
				String key = entry.getKey();
				ConnectionDefinition conn = entry.getValue();
				String url = conn.getUrl();
				String dbType = getDatabaseType(url);
				logger.debug(dbType + "->" + url);
				if (StringUtils.equals(dbType, "h2")) {
					dialects.put(key, new H2Dialect());
				} else if (StringUtils.equals(dbType, "mysql")) {
					dialects.put(key, new MySQLDialect());
				} else if (StringUtils.equals(dbType, "sqlserver")) {
					dialects.put(key, new SQLServer2008Dialect());
				} else if (StringUtils.equals(dbType, "sqlite")) {
					dialects.put(key, new SQLiteDialect());
				} else if (StringUtils.equals(dbType, "oracle")) {
					dialects.put(key, new OracleDialect());
				} else if (StringUtils.equals(dbType, "postgresql")) {
					dialects.put(key, new PostgreSQLDialect());
				} else if (StringUtils.equals(dbType, "db2")) {
					dialects.put(key, new DB2Dialect());
				}
			}
		}
		logger.debug("dialects->" + dialects);
		return dialects;
	}

	private static String getDatabaseType(String url) {
		String dbType = null;
		if (StringUtils.contains(url, "jdbc:mysql:")) {
			dbType = "mysql";
		} else if (StringUtils.contains(url, "jdbc:postgresql:")) {
			dbType = "postgresql";
		} else if (StringUtils.contains(url, "jdbc:h2:")) {
			dbType = "h2";
		} else if (StringUtils.contains(url, "jdbc:jtds:sqlserver:")) {
			dbType = "sqlserver";
		} else if (StringUtils.contains(url, "jdbc:sqlserver:")) {
			dbType = "sqlserver";
		} else if (StringUtils.contains(url, "jdbc:oracle:")) {
			dbType = "oracle";
		} else if (StringUtils.contains(url, "jdbc:db2:")) {
			dbType = "db2";
		} else if (StringUtils.contains(url, "jdbc:sqlite:")) {
			dbType = "sqlite";
		} else if (StringUtils.contains(url, "jdbc:phoenix:")) {
			dbType = "hbase";
		}
		return dbType;
	}

	public static String getDatabaseTypeByName(String systemName) {
		if (dbTypes.get(systemName) != null) {
			return dbTypes.get(systemName);
		}
		ConnectionDefinition conn = getConnectionDefinition(systemName);
		if (conn != null && conn.getType() != null) {
			return conn.getType();
		}
		return null;
	}

	public static Map<String, Properties> getDataSourceProperties() {
		Map<String, Properties> dsMap = new HashMap<String, Properties>();
		if (!dataSourceProperties.isEmpty()) {
			for (Entry<String, ConnectionDefinition> entry : dataSourceProperties.entrySet()) {
				String name = entry.getKey();
				ConnectionDefinition conn = entry.getValue();
				dsMap.put(name, toProperties(conn));
			}
		}
		return dsMap;
	}

	public static Properties getDataSourcePropertiesByName(String name) {
		if (name == null) {
			return null;
		}
		// logger.debug("->name:" + name);
		ConnectionDefinition conn = getConnectionDefinition(name);
        return toProperties(conn);
	}

	private static Properties getDefaultDataSourceProperties() {
		ConnectionDefinition conn = getConnectionDefinition(Environment.DEFAULT_SYSTEM_NAME);
		Properties props = toProperties(conn);
		if (props == null) {
			// 如果没有默认的jdbc配置，重装配置文件。
			reloadDS();
		}
		return props;
	}

	public static String getDefaultHibernateDialect() {
		Properties dialects = getHibernateDialectMappings();
		Properties props = getDefaultDataSourceProperties();
		if (props != null) {
			String dbType = props.getProperty(JDBC_TYPE);
			logger.debug("databaseType:" + dbType);
			return dialects.getProperty(dbType);
		}
		return null;
	}

	private static Properties getDialectMappings() {
		Properties dialectMappings = new Properties();
		dialectMappings.setProperty("h2", "com.glaf.core.dialect.H2Dialect");
		dialectMappings.setProperty("mysql", "com.glaf.core.dialect.MySQLDialect");
		dialectMappings.setProperty("oracle", "com.glaf.core.dialect.OracleDialect");
		dialectMappings.setProperty("postgresql", "com.glaf.core.dialect.PostgreSQLDialect");
		dialectMappings.setProperty("sqlserver", "com.glaf.core.dialect.SQLServer2008Dialect");
		dialectMappings.setProperty("sqlite", "com.glaf.core.dialect.SQLiteDialect");
		dialectMappings.setProperty("db2", "com.glaf.core.dialect.DB2Dialect");
		return dialectMappings;
	}

	private static Map<String, Dialect> getDialects() {
		Map<String, Dialect> dialects = new java.util.HashMap<String, Dialect>();
		dialects.put("h2", new H2Dialect());
		dialects.put("mysql", new MySQLDialect());
		dialects.put("sqlserver", new SQLServer2008Dialect());
		dialects.put("sqlite", new SQLiteDialect());
		dialects.put("oracle", new OracleDialect());
		dialects.put("postgresql", new PostgreSQLDialect());
		dialects.put("db2", new DB2Dialect());
		return dialects;
	}

	private static Properties getHibernateDialectMappings() {
		Properties dialectMappings = new Properties();
		dialectMappings.setProperty("h2", "org.hibernate.dialect.H2Dialect");
		dialectMappings.setProperty("mysql", "org.hibernate.dialect.MySQL5Dialect");
		dialectMappings.setProperty("oracle", "org.hibernate.dialect.Oracle10gDialect");
		dialectMappings.setProperty("postgresql", "org.hibernate.dialect.PostgreSQLDialect");
		dialectMappings.setProperty("sqlserver", "org.hibernate.dialect.SQLServerDialect");
		dialectMappings.setProperty("db2", "org.hibernate.dialect.DB2Dialect");
		return dialectMappings;
	}

	/**
	 * 获取主库数据源属性
	 * 
	 * @return
	 */
	public static Properties getMasterDataSourceProperties() {
		String configFile = SystemProperties.getMasterDataSourceConfigFile();
		String filename = SystemProperties.getConfigRootPath() + configFile;
        return PropertiesUtils.loadFilePathResource(filename);
	}

	public static Properties getProperties(String name) {
		if (name == null) {
			return null;
		}
		if (dataSourceProperties.isEmpty()) {
			reloadDS();
		}
		ConnectionDefinition conn = getConnectionDefinition(name);
        return toProperties(conn);
	}

	public static Properties getQueryRewriterMappings() {
		Properties dialectMappings = new Properties();
		dialectMappings.setProperty("h2", "org.apache.metamodel.jdbc.dialects.H2QueryRewriter");
		dialectMappings.setProperty("mysql", "org.apache.metamodel.jdbc.dialects.MysqlQueryRewriter");
		dialectMappings.setProperty("oracle", "org.apache.metamodel.jdbc.dialects.OracleQueryRewriter");
		dialectMappings.setProperty("postgresql", "org.apache.metamodel.jdbc.dialects.PostgresqlQueryRewriter");
		dialectMappings.setProperty("sqlserver", "org.apache.metamodel.jdbc.dialects.SQLServerQueryRewriter");
		dialectMappings.setProperty("db2", "org.apache.metamodel.jdbc.dialects.DB2QueryRewriter");
		return dialectMappings;
	}

	public static Properties getTemplateProperties(String name) {
		if (name == null) {
			return null;
		}
		if (jdbcTemplateProperties.isEmpty()) {
			init();
		}
		// logger.debug("name:" + name);
		Properties props = jdbcTemplateProperties.get(name);
		Properties p = new Properties();
		Enumeration<?> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			p.put(key, value);
		}
		return p;
	}

	private static void init() {
		if (!loading.get()) {
			try {
				loading.set(true);
				String config = SystemProperties.getConfigRootPath() + "/conf/templates/jdbc";
				File directory = new File(config);
				if (directory.exists() && directory.isDirectory()) {
					String[] filelist = directory.list();
					if (filelist != null) {
						for (String s : filelist) {
							String filename = config + "/" + s;
							File file = new File(filename);
							if (file.isFile() && file.getName().endsWith(".properties")) {
								InputStream inputStream = new FileInputStream(file);
								Properties props = PropertiesUtils.loadProperties(inputStream);
								if (props != null) {
									jdbcTemplateProperties.put(props.getProperty(JDBC_NAME), props);
								}
							}
						}
					}
				}
			} catch (Exception ex) {

				logger.error(ex);
			} finally {
				loading.set(false);
			}
		}
	}

	public static String isolationLevelToString(int isolation) {
		return ISOLATION_LEVELS.get(isolation);
	}

	private static void reloadDS() {
		Map<String, String> sysEnv = System.getenv();
		if (!loading.get()) {
			try {
				loading.set(true);
				/**
				 * 判断运行环境是否为docker
				 */
				if (!StringUtils.equals(sysEnv.get("PRD_RUN_ENV"), "docker")) {
					String path;
					String deploymentSystemName = SystemProperties.getDeploymentSystemName();
					if (deploymentSystemName != null && deploymentSystemName.length() > 0) {
						path = SystemProperties.getConfigRootPath() + Constants.DEPLOYMENT_JDBC_PATH
								+ deploymentSystemName + "/jdbc/";
					} else {
						path = SystemProperties.getConfigRootPath() + Constants.JDBC_CONFIG;
					}
					logger.info("datasource path:" + path);
					File dir = new File(path);
					if (dir.exists() && dir.isDirectory()) {
						File[] filelist = dir.listFiles();
						if (filelist != null) {
							for (File file : filelist) {
								if (file.getName().endsWith(".properties")) {
									logger.info("load jdbc properties:" + file.getAbsolutePath());
									try {
										Properties props = PropertiesUtils.loadProperties(new FileInputStream(file));
										if (JdbcConnectionFactory.checkConnection(props)) {
											String name = props.getProperty(JDBC_NAME);
											if (StringUtils.isNotEmpty(name)) {
												String dbType = props.getProperty(JDBC_TYPE);
												if (StringUtils.isEmpty(dbType)) {
													dbType = getDatabaseType(props.getProperty(JDBC_URL));
													props.setProperty(JDBC_TYPE, dbType);
												}
												ConnectionDefinition conn = toConnectionDefinition(props);
												dbTypes.put(name, dbType);
												dataSourceProperties.put(name, conn);
											}
										}
									} catch (Exception ex) {

										logger.error(ex);
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				logger.error(ex);
			} finally {
				loading.set(false);
			}
			/**
			 * 判断运行环境是否为docker，并且设置相关环境变量
			 */
			if (StringUtils.equals(sysEnv.get("PRD_RUN_ENV"), "docker")) {
				if (StringUtils.isNotEmpty(sysEnv.get("DB_TYPE")) && StringUtils.isNotEmpty(sysEnv.get("DB_HOST"))
						&& StringUtils.isNotEmpty(sysEnv.get("DB_PORT"))
						&& StringUtils.isNotEmpty(sysEnv.get("DB_DATABASE"))
						&& StringUtils.isNotEmpty(sysEnv.get("DB_USER"))) {
					Properties props = DBConfiguration.getTemplateProperties(sysEnv.get("DB_TYPE"));
					if (props != null && !props.isEmpty()) {
						Map<String, Object> context = new HashMap<String, Object>();
						context.put("host", sysEnv.get("DB_HOST"));
						context.put("port", sysEnv.get("DB_PORT"));
						context.put("databaseName", sysEnv.get("DB_DATABASE"));
						if (StringUtils.isNotEmpty(sysEnv.get("CONTEXT_PATH"))) {
							context.put("contextPath", sysEnv.get("CONTEXT_PATH"));
						} else {
							context.put("contextPath", "/glaf");
						}
						String driver = props.getProperty(DBConfiguration.JDBC_DRIVER);
						String url = props.getProperty(DBConfiguration.JDBC_URL);
						context.put("jdbc_user", sysEnv.get("DB_USER"));
						if (StringUtils.isNotEmpty(sysEnv.get("DB_PASSWORD"))) {
							context.put("jdbc_password", sysEnv.get("DB_PASSWORD"));
						} else if (StringUtils.isNotEmpty(sysEnv.get("DB_PASSWORD_CRYPT"))
								&& StringUtils.isNotEmpty(sysEnv.get("DB_KEY"))) {
							byte[] key = Hex.hex2byte(sysEnv.get("DB_KEY"));
							byte[] data = Hex.hex2byte(sysEnv.get("DB_PASSWORD_CRYPT"));
							try {
								byte[] bytes = AESUtils.decryptECB(key, data);
								context.put("jdbc_password", new String(bytes));
							} catch (Exception ignored) {
							}
						}
						url = ExpressionTools.evaluate(url, context);
						logger.debug("driver:" + driver);
						logger.debug("url:" + url);
						logger.debug("user:" + sysEnv.get("DB_USER"));
						context.put("jdbc_type", sysEnv.get("DB_TYPE"));
						context.put("jdbc_url", url);
						context.put("jdbc_driver", driver);

						boolean success = false;
						try {
							String filename = SystemProperties.getConfigRootPath()
									+ "/conf/templates/jdbc/jdbc.template.properties";
							File file = new File(filename);
							if (file.exists() && file.isFile()) {
								String content = new String(FileUtils.getBytes(file));
								String text = ExpressionTools.evaluate(content, context);
								filename = SystemProperties.getConfigRootPath()
										+ SystemProperties.getMasterDataSourceConfigFile();
								FileUtils.save(filename, text.getBytes());
								logger.info("从docker环境变量中获取数据库配置并保存成功。");
								loadDefaultJdbcProperties();
								success = true;
							}
						} catch (Exception ex) {
							success = false;
							logger.error(ex);
						}
						if (!success) {
							ConnectionDefinition conn = new ConnectionDefinition();
							conn.setDatabase(sysEnv.get("DB_DATABASE"));
							conn.setHost(sysEnv.get("DB_HOST"));
							conn.setName(Environment.DEFAULT_SYSTEM_NAME);
							conn.setPort(Integer.parseInt(sysEnv.get("DB_PORT")));
							conn.setUser(sysEnv.get("DB_USER"));
							if (StringUtils.isNotEmpty(sysEnv.get("DB_PASSWORD"))) {
								conn.setPassword(sysEnv.get("DB_PASSWORD"));
							} else if (StringUtils.isNotEmpty(sysEnv.get("DB_PASSWORD_CRYPT"))
									&& StringUtils.isNotEmpty(sysEnv.get("DB_KEY"))) {
								byte[] key = Hex.hex2byte(sysEnv.get("DB_KEY"));
								byte[] data = Hex.hex2byte(sysEnv.get("DB_PASSWORD_CRYPT"));
								try {
									byte[] bytes = AESUtils.decryptECB(key, data);
									conn.setPassword(new String(bytes));
								} catch (Exception ignored) {
								}
							}
							conn.setUrl(url);
							conn.setDriver(driver);
							DatabaseConnectionConfig cfg = new DatabaseConnectionConfig();
							if (cfg.checkConnection(conn)) {
								dataSourceProperties.put(Environment.DEFAULT_SYSTEM_NAME, conn);
								dbTypes.put(Environment.DEFAULT_SYSTEM_NAME, sysEnv.get("DB_TYPE"));
							}
						}
					}
				}
			} else {
				loadDefaultJdbcProperties();
			}
		}
	}

	private static void loadDefaultJdbcProperties() {
		String filename = SystemProperties.getConfigRootPath() + SystemProperties.getMasterDataSourceConfigFile();
		File file = new File(filename);
		if (file.exists() && file.isFile()) {
			logger.info("load default jdbc config:" + filename);
			Properties props = PropertiesUtils.loadFilePathResource(filename);
			String dbType = Objects.requireNonNull(props).getProperty(JDBC_TYPE);
			if (StringUtils.isEmpty(dbType)) {
				try {
					dbType = getDatabaseType(props.getProperty(JDBC_URL));
					if (dbType != null) {
						props.setProperty(JDBC_TYPE, dbType);
					}
				} catch (Exception ex) {

					logger.error(ex);
				}
			}
			ConnectionDefinition conn = toConnectionDefinition(props);
			dataSourceProperties.put(Environment.DEFAULT_SYSTEM_NAME, conn);
			if (dbType != null) {
				dbTypes.put(Environment.DEFAULT_SYSTEM_NAME, dbType);
			}
			jdbcTemplateProperties.put(Environment.DEFAULT_SYSTEM_NAME, props);
		}
		logger.info("#datasources:" + dataSourceProperties.keySet());
		if (!dataSourceProperties.containsKey(Environment.DEFAULT_SYSTEM_NAME)) {
			logger.warn("default jdbc properties not found!!!");
		}
	}

	private static ConnectionDefinition toConnectionDefinition(Properties props) {
		if (props != null && !props.isEmpty()) {
			ConnectionDefinition model = new ConnectionDefinition();

			model.setDriver(props.getProperty(JDBC_DRIVER));
			model.setUrl(props.getProperty(JDBC_URL));
			model.setName(props.getProperty(JDBC_NAME));
			model.setUser(props.getProperty(JDBC_USER));
			model.setPassword(props.getProperty(JDBC_PASSWORD));
			model.setSubject(props.getProperty(SUBJECT));
			model.setProvider(props.getProperty(JDBC_PROVIDER));
			model.setType(props.getProperty(JDBC_TYPE));
			model.setHost(props.getProperty(HOST));
			model.setDatabase(props.getProperty(DATABASE));

			if (StringUtils.isNotEmpty(props.getProperty(PORT))) {
				model.setPort(Integer.parseInt(props.getProperty(PORT)));
			}

			if (StringUtils.equals("true", props.getProperty(JDBC_AUTOCOMMIT))) {
				model.setAutoCommit(true);
			}

			Properties p = new Properties();
			Enumeration<?> e = props.keys();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = props.getProperty(key);
				p.put(key, value);
			}
			model.setProperties(p);
			return model;
		}
		return null;
	}

	private static Properties toProperties(ConnectionDefinition conn) {
		if (conn != null) {
			Properties props = new Properties();
			if (conn.getProperties() != null) {
				props.putAll(conn.getProperties());
			}
			if (conn.getSubject() != null) {
				props.setProperty(SUBJECT, conn.getSubject());
			}

			if (conn.getName() != null) {
				props.setProperty(JDBC_NAME, conn.getName());
			}
			props.setProperty(JDBC_DRIVER, conn.getDriver());
			props.setProperty(JDBC_URL, conn.getUrl());
			if (conn.getUser() != null) {
				props.setProperty(JDBC_USER, conn.getUser());
			}
			if (conn.getPassword() != null) {
				props.setProperty(JDBC_PASSWORD, conn.getPassword());
			}
			if (conn.getProvider() != null) {
				props.setProperty(JDBC_PROVIDER, conn.getProvider());
			}
			String type = getDatabaseType(conn.getUrl());
			props.setProperty(JDBC_TYPE, type);
			if (conn.getHost() != null) {
				props.setProperty(HOST, conn.getHost());
			}
			props.setProperty(PORT, String.valueOf(conn.getPort()));
			if (conn.getDatabase() != null) {
				props.setProperty(DATABASE, conn.getDatabase());
			}
			return props;
		}
		return null;
	}

	private DBConfiguration() {

	}

}