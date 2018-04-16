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

package com.glaf.core.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.glaf.core.context.ContextFactory;

public class DBConnectionFactory {

	protected final static Logger logger = LoggerFactory.getLogger(DBConnectionFactory.class);

	protected static Properties databaseTypeMappings = getDatabaseTypeMappings();

	protected static String databaseType;

	public static Connection getConnection() {
		DataSource dataSource = ContextFactory.getBean("dataSource");
		try {
			return dataSource.getConnection();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String getDatabaseType() {
		return databaseType;
	}

	public static String getDatabaseType(Connection connection) {
		if (connection != null) {
			String databaseProductName = null;
			try {
				DatabaseMetaData databaseMetaData = connection.getMetaData();
				databaseProductName = databaseMetaData.getDatabaseProductName();
				logger.debug("databaseProductName:" + databaseProductName);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
			String dbType = databaseTypeMappings.getProperty(databaseProductName);
			if (dbType == null) {
				throw new RuntimeException(
						"couldn't deduct database type from database product name '" + databaseProductName + "'");
			}
			return dbType;
		}
		return null;
	}

	public static String getDatabaseType(String url) {
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

	public static Properties getDatabaseTypeMappings() {
		Properties databaseTypeMappings = new Properties();
		databaseTypeMappings.setProperty("H2", "h2");
		databaseTypeMappings.setProperty("MySQL", "mysql");
		databaseTypeMappings.setProperty("Oracle", "oracle");
		databaseTypeMappings.setProperty("PostgreSQL", "postgresql");
		databaseTypeMappings.setProperty("Microsoft SQL Server", "sqlserver");
		databaseTypeMappings.setProperty("SQLite", "sqlite");
		databaseTypeMappings.setProperty("DB2", "db2");
		databaseTypeMappings.setProperty("DB2/NT", "db2");
		databaseTypeMappings.setProperty("DB2/NT64", "db2");
		databaseTypeMappings.setProperty("DB2 UDP", "db2");
		databaseTypeMappings.setProperty("DB2/LINUX", "db2");
		databaseTypeMappings.setProperty("DB2/LINUX390", "db2");
		databaseTypeMappings.setProperty("DB2/LINUXZ64", "db2");
		databaseTypeMappings.setProperty("DB2/LINUXX8664", "db2");
		databaseTypeMappings.setProperty("DB2/400 SQL", "db2");
		databaseTypeMappings.setProperty("DB2/6000", "db2");
		databaseTypeMappings.setProperty("DB2 UDB iSeries", "db2");
		databaseTypeMappings.setProperty("DB2/AIX64", "db2");
		databaseTypeMappings.setProperty("DB2/HPUX", "db2");
		databaseTypeMappings.setProperty("DB2/HP64", "db2");
		databaseTypeMappings.setProperty("DB2/SUN", "db2");
		databaseTypeMappings.setProperty("DB2/SUN64", "db2");
		databaseTypeMappings.setProperty("DB2/PTX", "db2");
		databaseTypeMappings.setProperty("DB2/2", "db2");

		return databaseTypeMappings;
	}

	public static void setDatabaseType(String databaseTypeX) {
		databaseType = databaseTypeX;
	}

}
