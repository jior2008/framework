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

package com.glaf.core.util;

import com.glaf.core.jdbc.DBConnectionFactory;
import com.glaf.core.model.ColumnDefinition;
import com.glaf.core.model.TableDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtils {

	private final static Log logger = LogFactory.getLog(DBUtils.class);

	public final static String newline = System.getProperty("line.separator");

	public static final String MYSQL = "mysql";

	public static final String ORACLE = "oracle";

	public static final String SQLITE = "sqlite";

	public static final String SQLSERVER = "sqlserver";

	public static final String POSTGRESQL = "postgresql";

	public static void alterTable(Connection connection, TableDefinition tableDefinition) {
		List<String> cloumns = new java.util.ArrayList<String>();
		Statement statement = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery("select * from " + tableDefinition.getTableName() + " where 1=0 ");
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String column = rsmd.getColumnName(i);
				cloumns.add(column.toUpperCase());
			}

			logger.debug(tableDefinition.getTableName() + " cloumns:" + cloumns);

			JdbcUtils.close(stmt);
			JdbcUtils.close(rs);

			Collection<ColumnDefinition> fields = tableDefinition.getColumns();
			for (ColumnDefinition field : fields) {
				if (field.getColumnName() != null && !cloumns.contains(field.getColumnName().toUpperCase())) {
					String sql = getAddColumnSql(dbType, tableDefinition.getTableName(), field);
					if (sql.length() > 0) {
						statement = connection.createStatement();
						logger.info("alter table " + tableDefinition.getTableName() + ":\n" + sql);
						statement.execute(sql);
						JdbcUtils.close(statement);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
			JdbcUtils.close(statement);
		}
	}

	public static void alterTable(String tableName, List<ColumnDefinition> columns) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSetMetaData rsmd;
		Statement stmt = null;
		ResultSet rs = null;
		List<String> columnNames = new java.util.ArrayList<String>();
		try {
			conn = DBConnectionFactory.getConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(" select * from " + tableName + " where 1=0 ");
			rs = pstmt.executeQuery();
			rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			for (int i = 1; i <= count; i++) {
				columnNames.add(rsmd.getColumnName(i).toLowerCase());
			}

			if (columns != null && !columns.isEmpty()) {
				String dbType = DBConnectionFactory.getDatabaseType(conn);
				for (ColumnDefinition column : columns) {
					if (columnNames.contains(column.getColumnName().toLowerCase())) {
						continue;
					}
					String javaType = column.getJavaType();
					String sql = " alter table " + tableName + " add " + column.getColumnName();
					if ("db2".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " varchar(" + column.getLength() + ")";
							} else {
								sql += " varchar(250) ";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " integer ";
						} else if ("Long".equals(javaType)) {
							sql += " bigint ";
						} else if ("Double".equals(javaType)) {
							sql += " double precision ";
						} else if ("Date".equals(javaType)) {
							sql += " timestamp ";
						} else if ("Clob".equals(javaType)) {
							sql += " clob(10240000) ";
						} else if ("Blob".equals(javaType)) {
							sql += " blob ";
						} else if ("byte[]".equals(javaType)) {
							sql += " blob ";
						} else if ("Boolean".equals(javaType)) {
							sql += " smallint ";
						}
					} else if ("oracle".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " NVARCHAR2(" + column.getLength() + ")";
							} else {
								sql += " NVARCHAR2(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " INTEGER ";
						} else if ("Long".equals(javaType)) {
							sql += " NUMBER(19,0) ";
						} else if ("Double".equals(javaType)) {
							sql += " NUMBER(*,10) ";
						} else if ("Date".equals(javaType)) {
							sql += " TIMESTAMP(6) ";
						} else if ("Clob".equals(javaType)) {
							sql += " CLOB ";
						} else if ("Blob".equals(javaType)) {
							sql += " BLOB ";
						} else if ("byte[]".equals(javaType)) {
							sql += " BLOB ";
						} else if ("Boolean".equals(javaType)) {
							sql += " NUMBER(1,0) ";
						}
					} else if ("mysql".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " varchar(" + column.getLength() + ")";
							} else {
								sql += " varchar(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " int  ";
						} else if ("Long".equals(javaType)) {
							sql += " bigint ";
						} else if ("Double".equals(javaType)) {
							sql += " double ";
						} else if ("Date".equals(javaType)) {
							sql += " datetime ";
						} else if ("Clob".equals(javaType)) {
							sql += " longtext ";
						} else if ("Blob".equals(javaType)) {
							sql += " longblob ";
						} else if ("byte[]".equals(javaType)) {
							sql += " longblob ";
						} else if ("Boolean".equals(javaType)) {
							sql += " tinyint ";
						}
					} else if ("postgresql".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " varchar(" + column.getLength() + ")";
							} else {
								sql += " varchar(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " integer ";
						} else if ("Long".equals(javaType)) {
							sql += " bigint ";
						} else if ("Double".equals(javaType)) {
							sql += " double precision ";
						} else if ("Date".equals(javaType)) {
							sql += " timestamp ";
						} else if ("Clob".equals(javaType)) {
							sql += " text ";
						} else if ("Blob".equals(javaType)) {
							sql += " bytea ";
						} else if ("byte[]".equals(javaType)) {
							sql += " bytea ";
						} else if ("Boolean".equals(javaType)) {
							sql += " boolean ";
						}
					} else if ("sqlserver".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " nvarchar(" + column.getLength() + ")";
							} else {
								sql += " nvarchar(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " int ";
						} else if ("Long".equals(javaType)) {
							sql += " numeric(19,0) ";
						} else if ("Double".equals(javaType)) {
							sql += " double precision ";
						} else if ("Date".equals(javaType)) {
							sql += " datetime ";
						} else if ("Clob".equals(javaType)) {
							sql += " nvarchar(max) ";
						} else if ("Blob".equals(javaType)) {
							sql += " varbinary(max) ";
						} else if ("byte[]".equals(javaType)) {
							sql += " varbinary(max) ";
						} else if ("Boolean".equals(javaType)) {
							sql += " tinyint ";
						}
					} else if ("h2".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " varchar(" + column.getLength() + ")";
							} else {
								sql += " varchar(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " int  ";
						} else if ("Long".equals(javaType)) {
							sql += " bigint ";
						} else if ("Double".equals(javaType)) {
							sql += " double ";
						} else if ("Date".equals(javaType)) {
							sql += " timestamp ";
						} else if ("Clob".equals(javaType)) {
							sql += " clob ";
						} else if ("Blob".equals(javaType)) {
							sql += " longvarbinary ";
						} else if ("byte[]".equals(javaType)) {
							sql += " longvarbinary ";
						} else if ("Boolean".equals(javaType)) {
							sql += " boolean ";
						}
					} else if ("sqlite".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " TEXT(" + column.getLength() + ")";
							} else {
								sql += " TEXT(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " INTEGER  ";
						} else if ("Long".equals(javaType)) {
							sql += " INTEGER ";
						} else if ("Double".equals(javaType)) {
							sql += " REAL ";
						} else if ("Date".equals(javaType)) {
							sql += " TEXT ";
						} else if ("Clob".equals(javaType)) {
							sql += " TEXT ";
						} else if ("Blob".equals(javaType)) {
							sql += " BLOB ";
						} else if ("byte[]".equals(javaType)) {
							sql += " BLOB ";
						}
					} else if ("hbase".equalsIgnoreCase(dbType)) {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " VARCHAR(" + column.getLength() + ")";
							} else {
								sql += " VARCHAR(250)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " INTEGER  ";
						} else if ("Long".equals(javaType)) {
							sql += " BIGINT ";
						} else if ("Double".equals(javaType)) {
							sql += " DOUBLE ";
						} else if ("Date".equals(javaType)) {
							sql += " TIMESTAMP ";
						} else if ("Clob".equals(javaType)) {
							sql += " VARCHAR ";
						} else if ("Blob".equals(javaType)) {
							sql += " VARBINARY ";
						} else if ("byte[]".equals(javaType)) {
							sql += " VARBINARY ";
						} else if ("Boolean".equals(javaType)) {
							sql += " BOOLEAN ";
						}
					} else {
						if ("String".equals(javaType)) {
							if (column.getLength() > 0) {
								sql += " varchar(" + column.getLength() + ")";
							} else {
								sql += " varchar(50)";
							}
						} else if ("Integer".equals(javaType)) {
							sql += " int ";
						} else if ("Long".equals(javaType)) {
							sql += " bigint ";
						} else if ("Double".equals(javaType)) {
							sql += " double ";
						} else if ("Date".equals(javaType)) {
							sql += " timestamp ";
						} else if ("Clob".equals(javaType)) {
							sql += " clob ";
						} else if ("Blob".equals(javaType)) {
							sql += " blob ";
						} else if ("byte[]".equals(javaType)) {
							sql += " blob ";
						} else if ("Boolean".equals(javaType)) {
							sql += " boolean ";
						}
					}
					logger.info("execute alter:" + sql);
					stmt = conn.createStatement();
					stmt.executeUpdate(sql);
					JdbcUtils.close(stmt);
				}
			}
			JdbcUtils.close(pstmt);
			conn.commit();
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
			JdbcUtils.close(pstmt);
			JdbcUtils.close(conn);
		}
	}

	public static void alterTable(TableDefinition tableDefinition) {
		List<String> cloumns = new java.util.ArrayList<String>();
		Connection connection = null;
		Statement statement = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = DBConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery("select * from " + tableDefinition.getTableName() + " where 1=0 ");
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String column = rsmd.getColumnName(i);
				cloumns.add(column.toUpperCase());
			}

			logger.debug(tableDefinition.getTableName() + " cloumns:" + cloumns);

			Collection<ColumnDefinition> fields = tableDefinition.getColumns();
			for (ColumnDefinition field : fields) {
				if (field.getColumnName() != null && !cloumns.contains(field.getColumnName().toUpperCase())) {
					String sql = getAddColumnSql(dbType, tableDefinition.getTableName(), field);
					if (sql.length() > 0) {
						statement = connection.createStatement();
						logger.info("alter table " + tableDefinition.getTableName() + ":\n" + sql);
						statement.execute(sql);
						JdbcUtils.close(statement);
					}
				}
			}
			connection.commit();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(stmt);
			JdbcUtils.close(rs);
			JdbcUtils.close(statement);
			JdbcUtils.close(connection);
		}
	}

	public static void createIndex(Connection connection, String tableName, String columnName, String indexName) {
		DatabaseMetaData dbmd;
		Statement stmt = null;
		ResultSet rs = null;
		boolean hasIndex = false;
		boolean autoCommit;
		try {
			autoCommit = connection.getAutoCommit();
			dbmd = connection.getMetaData();
			rs = dbmd.getIndexInfo(null, null, tableName, false, false);
			while (rs.next()) {
				String col = rs.getString("COLUMN_NAME");
				if (StringUtils.equalsIgnoreCase(columnName, col)) {
					hasIndex = true;
					break;
				}
			}
			JdbcUtils.close(rs);

			if (!hasIndex) {
				String sql = " create index " + indexName.toUpperCase() + " on " + tableName + " (" + columnName + ") ";
				connection.setAutoCommit(false);
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				JdbcUtils.close(stmt);
				connection.commit();
				connection.setAutoCommit(autoCommit);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
		}
	}

	public static void createIndex(String tableName, String columnName, String indexName) {
		Connection connection = null;
		DatabaseMetaData dbmd;
		Statement stmt = null;
		ResultSet rs = null;
		boolean hasIndex = false;
		try {
			connection = DBConnectionFactory.getConnection();
			dbmd = connection.getMetaData();
			rs = dbmd.getIndexInfo(null, null, tableName, false, false);
			while (rs.next()) {
				String col = rs.getString("COLUMN_NAME");
				if (StringUtils.equalsIgnoreCase(columnName, col)) {
					hasIndex = true;
					break;
				}
			}
			JdbcUtils.close(rs);

			if (!hasIndex) {
				String sql = " create index " + indexName.toUpperCase() + " on " + tableName + " (" + columnName + ") ";
				connection.setAutoCommit(false);
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				JdbcUtils.close(stmt);
				connection.commit();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
			JdbcUtils.close(connection);
		}
	}

	/**
	 * 创建数据库表，如果已经存在，则不重建
	 * 
	 * @param connection      JDBC连接
	 * @param tableDefinition 表定义
	 */
	public static String createTable(Connection connection, TableDefinition tableDefinition) {
		Statement statement = null;
		try {
			String tableName = tableDefinition.getTableName();
			if (tableExists(connection, tableName)) {
				return null;
			}
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			logger.info("dbType:" + dbType);
			String sql = getCreateTableScript(dbType, tableDefinition);
			if (sql.length() > 0) {
				connection.setAutoCommit(false);
				statement = connection.createStatement();
				logger.info("create table " + tableDefinition.getTableName() + ":\n" + sql);
				statement.execute(sql.replaceAll("\n", ""));
				JdbcUtils.close(statement);
				connection.commit();
				return sql;
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(statement);
		}
	}

	public static String createTable(TableDefinition tableDefinition) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = DBConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			String sql = getCreateTableScript(dbType, tableDefinition);
			if (sql.length() > 0) {
				statement = connection.createStatement();
				logger.info("create table " + tableDefinition.getTableName() + ":\n" + sql);
				statement.execute(sql.replaceAll("\n", ""));
				JdbcUtils.close(statement);
				connection.commit();
				return sql;
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(statement);
			JdbcUtils.close(connection);
		}
	}

	/**
	 * 创建数据库表，如果已经存在，则删除重建
	 * 
	 * @param connection      JDBC连接
	 * @param tableDefinition 表定义
	 */
	public static void dropAndCreateTable(Connection connection, TableDefinition tableDefinition) {
		String tableName = tableDefinition.getTableName();
		if (tableExists(connection, tableName)) {
			dropTable(connection, tableName);
		}
		if (!tableExists(connection, tableName)) {
			createTable(connection, tableDefinition);
		}
	}

	/**
	 * 如果已经存在，则删除
	 * 
	 * @param connection JDBC连接
	 * @param tableName  表名称
	 */
	public static void dropTable(Connection connection, String tableName) {
		Statement statement = null;
		try {
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			/**
			 * 只能在开发模式下才能删除表，正式环境只能删除临时表。
			 */
			if (System.getProperty("devMode") != null || StringUtils.equalsIgnoreCase(dbType, "sqlite")
					|| StringUtils.equalsIgnoreCase(dbType, "h2") || StringUtils.startsWithIgnoreCase(tableName, "temp")
					|| StringUtils.startsWithIgnoreCase(tableName, "tmp")) {
				if (tableExists(connection, tableName)) {
					statement = connection.createStatement();
					statement.executeUpdate(" drop table " + tableName);
					JdbcUtils.close(statement);
					logger.info("drop table:" + tableName);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(statement);
		}
	}

	/**
	 * 如果已经存在，则删除
	 *
	 * @param tableName 表名称
	 */
	public static void dropTable(String tableName) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = DBConnectionFactory.getConnection();
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			/**
			 * 只能在开发模式下才能删除表，正式环境只能删除临时表。
			 */
			if (System.getProperty("devMode") != null || StringUtils.equalsIgnoreCase(dbType, "sqlite")
					|| StringUtils.equalsIgnoreCase(dbType, "h2") || StringUtils.startsWithIgnoreCase(tableName, "temp")
					|| StringUtils.startsWithIgnoreCase(tableName, "tmp")) {
				if (tableExists(connection, tableName)) {
					connection.setAutoCommit(false);
					statement = connection.createStatement();
					statement.executeUpdate(" drop table " + tableName);
					JdbcUtils.close(statement);
					connection.commit();
					logger.info("drop table:" + tableName);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(statement);
			JdbcUtils.close(connection);
		}
	}

	/**
	 * 删除临时表数据
	 * 
	 * @param connection
	 * @param tableName
	 */
	public static void emptyTable(Connection connection, String tableName) {
		if (StringUtils.startsWithIgnoreCase(tableName, "temp") || StringUtils.startsWithIgnoreCase(tableName, "tmp")) {
			Statement statement = null;
			try {
				if (tableExists(connection, tableName)) {
					statement = connection.createStatement();
					statement.executeUpdate(" delete from " + tableName);
					JdbcUtils.close(statement);
					logger.info("empty table:" + tableName);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				JdbcUtils.close(statement);
			}
		}
	}

	/**
	 * 删除临时表数据
	 * 
	 * @param tableName
	 */
	public static void emptyTable(String tableName) {
		if (StringUtils.startsWithIgnoreCase(tableName, "temp") || StringUtils.startsWithIgnoreCase(tableName, "tmp")) {
			Connection connection = null;
			Statement statement = null;
			try {
				connection = DBConnectionFactory.getConnection();
				if (tableExists(connection, tableName)) {
					connection.setAutoCommit(false);
					statement = connection.createStatement();
					statement.executeUpdate(" delete from " + tableName);
					connection.commit();
					JdbcUtils.close(statement);
					logger.info("empty table:" + tableName);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				JdbcUtils.close(connection);
				JdbcUtils.close(statement);
			}
		}
	}

	public static void executeBatchSchemaResourceIgnoreException(Connection conn, String ddlStatements) {
		Statement statement = null;
		String sqlStatement = null;
		try {
			statement = conn.createStatement();
			StringTokenizer tokenizer = new StringTokenizer(ddlStatements, ";");
			while (tokenizer.hasMoreTokens()) {
				sqlStatement = tokenizer.nextToken();
				if (StringUtils.isNotEmpty(sqlStatement) && !sqlStatement.startsWith("#")) {
					// logger.debug(sqlStatement);
					try {
						// statement.executeUpdate(sqlStatement);
						statement.addBatch(sqlStatement);
					} catch (Exception ex) {
						// logger.error(" execute statement error: " + sqlStatement, ex);
					}
				}
			}
			statement.executeBatch();
		} catch (Exception ex) {
			throw new RuntimeException("execute statement error: " + sqlStatement, ex);
		} finally {
			JdbcUtils.close(statement);
		}
	}

	public static void executeSchemaResource(Connection conn, String ddlStatements) {
		Exception exception = null;
		Statement statement = null;
		String sqlStatement = null;
		try {
			StringTokenizer tokenizer = new StringTokenizer(ddlStatements, ";");
			while (tokenizer.hasMoreTokens()) {
				sqlStatement = tokenizer.nextToken();
				if (StringUtils.isNotEmpty(sqlStatement) && !sqlStatement.startsWith("#")) {
					logger.debug(sqlStatement);
					try {
						statement = conn.createStatement();
						statement.executeUpdate(sqlStatement);
						JdbcUtils.close(statement);
					} catch (Exception ex) {
						if (exception == null) {
							exception = ex;
						}
						logger.debug(" execute statement error: " + sqlStatement, ex);
					} finally {
						JdbcUtils.close(statement);
					}
				}
			}

			if (exception != null) {
				exception.printStackTrace();
				throw exception;
			}

			logger.info("execute statement successful");

		} catch (Exception ex) {
			throw new RuntimeException("execute statement error: " + sqlStatement, ex);
		} finally {
			JdbcUtils.close(statement);
		}
	}

	public static void executeSchemaResourceIgnoreException(Connection conn, String ddlStatements) {
		Statement statement = null;
		String sqlStatement = null;
		try {
			StringTokenizer tokenizer = new StringTokenizer(ddlStatements, ";");
			while (tokenizer.hasMoreTokens()) {
				sqlStatement = tokenizer.nextToken();
				if (StringUtils.isNotEmpty(sqlStatement) && !sqlStatement.startsWith("#")) {
					logger.debug(sqlStatement);
					statement = conn.createStatement();
					try {
						statement.executeUpdate(sqlStatement);
					} catch (Exception ex) {
						logger.error(" execute statement error: " + sqlStatement, ex);
					} finally {
						JdbcUtils.close(statement);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("couldn't execute db schema: " + sqlStatement, ex);
		} finally {
			JdbcUtils.close(statement);
		}
	}

	public static String executeSchemaResourceIgnoreException(String ddlStatements) {
		StringBuilder buffer = new StringBuilder();
		Connection connection = null;
		Statement statement = null;
		String sqlStatement;
		try {
			connection = DBConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			StringTokenizer tokenizer = new StringTokenizer(ddlStatements, ";");
			while (tokenizer.hasMoreTokens()) {
				sqlStatement = tokenizer.nextToken().trim();
				if (StringUtils.isNotEmpty(sqlStatement) && !sqlStatement.startsWith("#")) {
					// logger.debug(sqlStatement);
					try {
						statement = connection.createStatement();
						statement.executeUpdate(sqlStatement);
						connection.commit();
					} catch (Exception ex) {
						buffer.append(FileUtils.newline);
						buffer.append(sqlStatement).append(";");
						buffer.append(FileUtils.newline);
					} finally {
						JdbcUtils.close(statement);
					}
				}
			}
			return buffer.toString();
		} catch (Exception ex) {

			throw new RuntimeException("can't get connection: ", ex);
		} finally {
			JdbcUtils.close(statement);
			JdbcUtils.close(connection);
		}
	}

	public static String getAddColumnSql(String dbType, String tableName, ColumnDefinition field) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" alter table ").append(tableName);
		buffer.append(" add ").append(field.getColumnName());
		if ("h2".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" double ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" clob ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" longvarbinary ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" longvarbinary ");
			} else if ("Boolean".equals(field.getJavaType())) {
				buffer.append(" boolean ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" varchar ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("db2".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" clob (10240000) ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" blob ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" blob ");
			} else if ("Boolean".equals(field.getJavaType())) {
				buffer.append(" smallint ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" varchar ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("oracle".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" NUMBER(19,0) ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" NUMBER(*,10) ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" TIMESTAMP(6) ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" CLOB ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("Boolean".equals(field.getJavaType())) {
				buffer.append(" NUMBER(1,0) ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" NVARCHAR2 ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("mysql".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" double ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" datetime ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" longtext ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" longblob ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" longblob ");
			} else if ("Boolean".equals(field.getJavaType())) {
				buffer.append(" tinyint ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" varchar");
				if (field.getLength() > 0) {
					buffer.append("(").append(field.getLength()).append(") ");
				} else {
					buffer.append("(250) ");
				}
			}
		} else if ("sqlserver".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" datetime ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" nvarchar(max) ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" varbinary(max) ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" varbinary(max) ");
			} else if ("Boolean".equals(field.getJavaType())) {
				buffer.append(" tinyint ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" nvarchar ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if (POSTGRESQL.equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" text ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" bytea ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" bytea ");
			} else if ("Boolean".equals(field.getJavaType())) {
				buffer.append(" boolean ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" varchar ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("sqlite".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" REAL ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" TEXT ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" TEXT ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" TEXT ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("hbase".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(field.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(field.getJavaType())) {
				buffer.append(" BIGINT ");
			} else if ("Double".equals(field.getJavaType())) {
				buffer.append(" DOUBLE ");
			} else if ("Date".equals(field.getJavaType())) {
				buffer.append(" TIMESTAMP ");
			} else if ("Clob".equals(field.getJavaType())) {
				buffer.append(" VARCHAR ");
			} else if ("Blob".equals(field.getJavaType())) {
				buffer.append(" VARBINARY ");
			} else if ("byte[]".equals(field.getJavaType())) {
				buffer.append(" VARBINARY ");
			} else if ("String".equals(field.getJavaType())) {
				buffer.append(" VARCHAR ");
				if (field.getLength() > 0) {
					buffer.append(" (").append(field.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else {
			throw new RuntimeException(dbType + " is not support database type.");
		}

		buffer.append(";");

		return buffer.toString();
	}

	public static String getAlterTable(TableDefinition classDefinition) {
		StringBuilder buffer = new StringBuilder();
		List<String> cloumns = new java.util.ArrayList<String>();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = DBConnectionFactory.getConnection();
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			stmt = connection.createStatement();
			rs = stmt.executeQuery("select * from " + classDefinition.getTableName() + " where 1=0 ");
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String column = rsmd.getColumnName(i);
				cloumns.add(column.toUpperCase());
			}

			Collection<ColumnDefinition> fields = classDefinition.getColumns();
			for (ColumnDefinition field : fields) {
				if (field.getColumnName() != null && !cloumns.contains(field.getColumnName().toUpperCase())) {
					String str = getAddColumnSql(dbType, classDefinition.getTableName(), field);
					buffer.append(str);
					buffer.append("\r\r");
				}
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
			JdbcUtils.close(connection);
		}

		return buffer.toString();
	}

	public static List<ColumnDefinition> getColumnDefinitions(Connection conn, String tableName) {
		List<ColumnDefinition> columns = new java.util.ArrayList<ColumnDefinition>();
		ResultSet rs = null;
		try {
			List<String> primaryKeys = getPrimaryKeys(conn, tableName);
			String dbType = DBConnectionFactory.getDatabaseType(conn);
			DatabaseMetaData metaData = conn.getMetaData();
			if ("h2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("oracle".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("db2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("mysql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			} else if ("postgresql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			}
			rs = metaData.getColumns(null, null, tableName, null);
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				String typeName = rs.getString("TYPE_NAME");
				int dataType = rs.getInt("DATA_TYPE");
				int nullable = rs.getInt("NULLABLE");
				int length = rs.getInt("COLUMN_SIZE");
				int ordinal = rs.getInt("ORDINAL_POSITION");
				ColumnDefinition column = new ColumnDefinition();
				column.setColumnName(columnName.toLowerCase());
				column.setTitle(column.getName());
				column.setEnglishTitle(column.getName());
				column.setJavaType(FieldType.getJavaType(dataType));
				column.setName(StringTools.camelStyle(column.getColumnName().toLowerCase()));
				if (nullable == 1) {
					column.setNullable(true);
				} else {
					column.setNullable(false);
				}
				column.setLength(length);
				column.setOrdinal(ordinal);

				if ("String".equals(column.getJavaType())) {
					if (column.getLength() > 8000) {
						column.setJavaType("Clob");
					}
				}

				if ("Double".equals(column.getJavaType())) {
					if (column.getLength() == 19) {
						column.setJavaType("Long");
					}
				}

				if (StringUtils.equalsIgnoreCase(typeName, "bool") || StringUtils.equalsIgnoreCase(typeName, "boolean")
						|| StringUtils.equalsIgnoreCase(typeName, "bit")
						|| StringUtils.equalsIgnoreCase(typeName, "tinyint")
						|| StringUtils.equalsIgnoreCase(typeName, "smallint")) {
					column.setJavaType("Boolean");
				}

				if (primaryKeys.contains(columnName.toLowerCase())) {
					column.setPrimaryKey(true);
				}

				if (!columns.contains(column)) {
					logger.debug("column name:" + column.getColumnName());
					columns.add(column);
				}
			}

			return columns;
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
		}
	}

	public static List<ColumnDefinition> getColumnDefinitions(String tableName) {
		List<ColumnDefinition> columns = new java.util.ArrayList<ColumnDefinition>();
		Connection conn = null;
		ResultSet rs = null;
		try {
			List<String> primaryKeys = getPrimaryKeys(tableName);

			conn = DBConnectionFactory.getConnection();
			String dbType = DBConnectionFactory.getDatabaseType(conn);

			DatabaseMetaData metaData = conn.getMetaData();
			if ("h2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("oracle".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("db2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("mysql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			} else if ("postgresql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			}
			rs = metaData.getColumns(null, null, tableName, null);
			while (rs.next()) {
				String name = rs.getString("COLUMN_NAME");
				String typeName = rs.getString("TYPE_NAME");
				int dataType = rs.getInt("DATA_TYPE");
				int nullable = rs.getInt("NULLABLE");
				int length = rs.getInt("COLUMN_SIZE");
				int ordinal = rs.getInt("ORDINAL_POSITION");

				ColumnDefinition column = new ColumnDefinition();
				column.setColumnName(name);
				column.setJavaType(FieldType.getJavaType(dataType));
				if (nullable == 1) {
					column.setNullable(true);
				} else {
					column.setNullable(false);
				}
				column.setLength(length);
				column.setOrdinal(ordinal);
				column.setName(StringTools.camelStyle(column.getColumnName().toLowerCase()));

				logger.debug(name + " typeName:" + typeName + "[" + dataType + "] " + FieldType.getJavaType(dataType));

				if ("String".equals(column.getJavaType())) {
					if (column.getLength() > 8000) {
						column.setJavaType("Clob");
					}
				}

				if ("Double".equals(column.getJavaType())) {
					if (column.getLength() == 19) {
						column.setJavaType("Long");
					}
				}

				if (StringUtils.equalsIgnoreCase(typeName, "bool") || StringUtils.equalsIgnoreCase(typeName, "boolean")
						|| StringUtils.equalsIgnoreCase(typeName, "bit")
						|| StringUtils.equalsIgnoreCase(typeName, "tinyint")
						|| StringUtils.equalsIgnoreCase(typeName, "smallint")) {
					column.setJavaType("Boolean");
				}

				if (primaryKeys.contains(name) || primaryKeys.contains(name.toUpperCase())
						|| primaryKeys.contains(name.toLowerCase())) {
					column.setPrimaryKey(true);
				}

				if (!columns.contains(column)) {
					columns.add(column);
				}
			}

			return columns;
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(conn);
		}
	}

	public static String getColumnScript(String dbType, ColumnDefinition column) {
		StringBuilder buffer = new StringBuilder(500);
		buffer.append(newline);
		buffer.append("    ").append(column.getColumnName().toUpperCase());
		if ("db2".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" clob (10240000) ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" blob ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" blob ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" smallint ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" varchar ");
				if (column.getLength() > 0 && column.getLength() <= 4000) {
					buffer.append(" (").append(column.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("oracle".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" NUMBER(19,0) ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" NUMBER(*,10) ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" TIMESTAMP(6) ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" CLOB ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" NUMBER(1,0) ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" VARCHAR2 ");
				if (column.getLength() > 0 && column.getLength() <= 4000) {
					buffer.append(" (").append(column.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("mysql".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" double ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" datetime ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" longtext ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" longblob ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" longblob ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" tinyint ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" varchar ");
				if (column.getLength() > 0 && column.getLength() <= 4000) {
					buffer.append(" (").append(column.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("sqlserver".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" datetime ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" nvarchar(max) ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" varbinary(max) ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" varbinary(max) ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" tinyint ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" nvarchar ");
				if (column.getLength() > 0 && column.getLength() <= 4000) {
					buffer.append(" (").append(column.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("postgresql".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" text ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" bytea ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" bytea ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" boolean ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" varchar ");
				if (column.getLength() > 0 && column.getLength() <= 4000) {
					buffer.append(" (").append(column.getLength()).append(") ");
				} else {
					buffer.append(" (250) ");
				}
			}
		} else if ("h2".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" double ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" clob ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" longvarbinary ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" longvarbinary ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" boolean ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" varchar ");
				if (column.getLength() > 0 && column.getLength() <= 4000) {
					buffer.append(" (").append(column.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("sqlite".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" REAL ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" TEXT ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" TEXT ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" BLOB ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" TEXT ");
			}
		} else if ("hbase".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(column.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Boolean".equals(column.getJavaType())) {
				buffer.append(" BOOLEAN ");
			} else if ("Long".equals(column.getJavaType())) {
				buffer.append(" BIGINT ");
			} else if ("Double".equals(column.getJavaType())) {
				buffer.append(" DOUBLE ");
			} else if ("Date".equals(column.getJavaType())) {
				buffer.append(" TIMESTAMP ");
			} else if ("Clob".equals(column.getJavaType())) {
				buffer.append(" VARCHAR ");
			} else if ("Blob".equals(column.getJavaType())) {
				buffer.append(" VARBINARY ");
			} else if ("byte[]".equals(column.getJavaType())) {
				buffer.append(" VARBINARY ");
			} else if ("String".equals(column.getJavaType())) {
				buffer.append(" VARCHAR ");
			}
		}

		buffer.append(",");
		return buffer.toString();
	}

	public static String getCreateTableDDL() {
		StringBuilder buffer = new StringBuilder();
		List<String> tables = getTables();
		String dbType = DBConnectionFactory.getDatabaseType();
		if (tables != null && !tables.isEmpty()) {
			for (String tableName : tables) {
				List<ColumnDefinition> columns = getColumnDefinitions(tableName);
				TableDefinition tableDefinition = new TableDefinition();
				tableDefinition.setTableName(tableName);
				List<String> pks = getPrimaryKeys(tableName);
				if (pks != null && columns != null && !columns.isEmpty()) {
					for (ColumnDefinition c : columns) {
						if (pks.contains(c.getColumnName())) {
							c.setPrimaryKey(true);
							tableDefinition.setIdColumn(c);
						}
					}
				}
				tableDefinition.setColumns(columns);
				String str = getCreateTableScript(dbType, tableDefinition);
				buffer.append(str);
				buffer.append(FileUtils.newline);
				buffer.append(FileUtils.newline);
			}
		}
		return buffer.toString();
	}

	public static String getCreateTableDDL(String targetDbType) {
		StringBuilder buffer = new StringBuilder();
		List<String> tables = getTables();

		if (tables != null && !tables.isEmpty()) {
			for (String tableName : tables) {
				List<ColumnDefinition> columns = getColumnDefinitions(tableName);
				TableDefinition tableDefinition = new TableDefinition();
				tableDefinition.setTableName(tableName);
				List<String> pks = getPrimaryKeys(tableName);
				if (pks != null && columns != null && !columns.isEmpty()) {
					for (ColumnDefinition c : columns) {
						if (pks.contains(c.getColumnName())) {
							c.setPrimaryKey(true);
							tableDefinition.setIdColumn(c);
						}
					}
				}
				tableDefinition.setColumns(columns);
				String str = getCreateTableScript(targetDbType, tableDefinition);
				buffer.append(str);
				buffer.append(FileUtils.newline);
				buffer.append(FileUtils.newline);
			}
		}
		return buffer.toString();
	}

	public static String getCreateTableScript(String dbType, TableDefinition tableDefinition) {
		StringBuilder buffer = new StringBuilder(4000);
		Collection<ColumnDefinition> columns = tableDefinition.getColumns();
		buffer.append(" create table ").append(tableDefinition.getTableName().toUpperCase());
		buffer.append(" ( ");
		Collection<String> cols = new HashSet<String>();
		List<ColumnDefinition> idColumns = tableDefinition.getIdColumns();
		ColumnDefinition idColumn = tableDefinition.getIdColumn();
		if (idColumns != null && !idColumns.isEmpty()) {
			for (ColumnDefinition col : idColumns) {
				buffer.append(getPrimaryKeyScript(dbType, col));
				cols.add(col.getColumnName().trim().toLowerCase());
			}
		} else if (idColumn != null) {
			buffer.append(getPrimaryKeyScript(dbType, idColumn));
			cols.add(idColumn.getColumnName().trim().toLowerCase());
		}

		for (ColumnDefinition column : columns) {
			if (cols.contains(column.getColumnName().trim().toLowerCase())) {
				continue;
			}
			buffer.append(getColumnScript(dbType, column));
			cols.add(column.getColumnName().trim().toLowerCase());
		}

		if ("hbase".equalsIgnoreCase(dbType)) {
			if (idColumn != null) {
				buffer.append(newline);
				buffer.append("  CONSTRAINT pk PRIMARY KEY (").append(idColumn.getColumnName().toUpperCase())
						.append(") ");
			}
		} else {
			if (tableDefinition.getIdColumns() != null && !tableDefinition.getIdColumns().isEmpty()) {
				buffer.append(newline);
				buffer.append("  primary key (");
				for (ColumnDefinition col : tableDefinition.getIdColumns()) {
					buffer.append(col.getColumnName().toUpperCase()).append(", ");
				}
				buffer.delete(buffer.length() - 2, buffer.length());
				buffer.append(") ");
			} else if (tableDefinition.getIdColumn() != null) {
				buffer.append(newline);
				buffer.append("  primary key (").append(tableDefinition.getIdColumn().getColumnName().toUpperCase())
						.append(") ");
			}
		}

		if (buffer.toString().endsWith(",")) {
			buffer.delete(buffer.length() - 1, buffer.length());
		}

		buffer.append(newline);
		if ("mysql".equalsIgnoreCase(dbType)) {
			buffer.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;");
		} else if ("oracle".equalsIgnoreCase(dbType)) {
			buffer.append(")");
		} else {
			buffer.append(");");
		}

		return buffer.toString();
	}

	public static List<String> getPrimaryKeys(Connection connection, String tableName) {
		ResultSet rs = null;
		List<String> primaryKeys = new java.util.ArrayList<String>();
		try {
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			DatabaseMetaData metaData = connection.getMetaData();

			if ("h2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("oracle".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("db2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("mysql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			} else if (POSTGRESQL.equals(dbType)) {
				tableName = tableName.toLowerCase();
			}

			rs = metaData.getPrimaryKeys(null, null, tableName);
			while (rs.next()) {
				primaryKeys.add(rs.getString("column_name").toLowerCase());
			}

			// logger.debug(tableName + " primaryKeys:" + primaryKeys);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
		}
		return primaryKeys;
	}

	public static List<String> getPrimaryKeys(String tableName) {
		List<String> primaryKeys = new java.util.ArrayList<String>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBConnectionFactory.getConnection();
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			DatabaseMetaData metaData = connection.getMetaData();

			if ("h2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("oracle".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("db2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("mysql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			} else if (POSTGRESQL.equals(dbType)) {
				tableName = tableName.toLowerCase();
			}

			rs = metaData.getPrimaryKeys(null, null, tableName);
			while (rs.next()) {
				primaryKeys.add(rs.getString("column_name"));
			}

			// logger.debug(tableName + " primaryKeys:" + primaryKeys);
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(connection);
		}
		return primaryKeys;
	}

	public static String getPrimaryKeyScript(String dbType, ColumnDefinition idField) {
		StringBuilder buffer = new StringBuilder(500);

		buffer.append(newline);
		buffer.append("    ").append(idField.getColumnName().toUpperCase());
		if ("db2".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" varchar ");
				if (idField.getLength() > 0) {
					buffer.append(" (").append(idField.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("oracle".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" NUMBER(19,0) ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" NUMBER(*,10) ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" TIMESTAMP(6) ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" VARCHAR2 ");
				if (idField.getLength() > 0) {
					buffer.append(" (").append(idField.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("mysql".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" double ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" datetime ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" varchar ");
				if (idField.getLength() > 0) {
					buffer.append(" (").append(idField.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("sqlserver".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" datetime ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" nvarchar ");
				if (idField.getLength() > 0) {
					buffer.append(" (").append(idField.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("postgresql".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" double precision ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" varchar ");
				if (idField.getLength() > 0) {
					buffer.append(" (").append(idField.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("h2".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" integer ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" bigint ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" double ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" timestamp ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" varchar ");
				if (idField.getLength() > 0) {
					buffer.append(" (").append(idField.getLength()).append(") ");
				} else {
					buffer.append(" (50) ");
				}
			}
		} else if ("sqlite".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" REAL ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" TEXT ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" TEXT ");
			}
		} else if ("hbase".equalsIgnoreCase(dbType)) {
			if ("Integer".equals(idField.getJavaType())) {
				buffer.append(" INTEGER ");
			} else if ("Long".equals(idField.getJavaType())) {
				buffer.append(" BIGINT ");
			} else if ("Double".equals(idField.getJavaType())) {
				buffer.append(" DOUBLE ");
			} else if ("Date".equals(idField.getJavaType())) {
				buffer.append(" TIMESTAMP ");
			} else if ("String".equals(idField.getJavaType())) {
				buffer.append(" VARCHAR ");
			}
		}
		buffer.append(" not null, ");
		return buffer.toString();
	}

	/**
	 * 获取保密表
	 * 
	 * @return
	 */
	public static List<String> getSecretTables() {
		List<String> tables = new ArrayList<String>();
		tables.add("userinfo");
		tables.add("sys_user");
		tables.add("SYS_KEY");
		tables.add("SYS_SERVER");
		tables.add("SYS_PROPERTY");
		return tables;
	}

	public static int getTableCount(Connection connection, String tableName) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(" select count(*) from " + tableName);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
		}
		return -1;
	}

	public static int getTableCount(String tableName) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = DBConnectionFactory.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(" select count(*) from " + tableName);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(stmt);
			JdbcUtils.close(connection);
		}
		return -1;
	}

	public static List<String> getTables() {
		List<String> tables = new java.util.ArrayList<String>();
		String[] types = { "TABLE" };
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBConnectionFactory.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			rs = metaData.getTables(null, null, null, types);
			while (rs.next()) {
				tables.add(rs.getObject("TABLE_NAME").toString().toLowerCase());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(connection);
		}
		return tables;
	}

	public static List<String> getTables(Connection connection) {
		List<String> tables = new java.util.ArrayList<String>();
		String[] types = { "TABLE" };
		ResultSet rs = null;
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			rs = metaData.getTables(null, null, null, types);
			while (rs.next()) {
				tables.add(rs.getObject("TABLE_NAME").toString().toLowerCase());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
		}
		return tables;
	}

	/**
	 * 获取保密表
	 * 
	 * @return
	 */
	public static List<String> getUnWritableSecretTables() {
		List<String> tables = new ArrayList<String>();
		tables.add("USERINFO");
		tables.add("SYS_USER");
		tables.add("SYS_KEY");
		tables.add("SYS_SERVER");
		tables.add("SYS_PROPERTY");
		tables.add("SYS_DATABASE");
		tables.add("SYS_LOGIN_INFO");
		return tables;
	}

	public static List<String> getUpperCasePrimaryKeys(String tableName) {
		List<String> primaryKeys = new java.util.ArrayList<String>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBConnectionFactory.getConnection();
			String dbType = DBConnectionFactory.getDatabaseType(connection);
			DatabaseMetaData metaData = connection.getMetaData();

			if ("h2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("oracle".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("db2".equals(dbType)) {
				tableName = tableName.toUpperCase();
			} else if ("mysql".equals(dbType)) {
				tableName = tableName.toLowerCase();
			} else if (POSTGRESQL.equals(dbType)) {
				tableName = tableName.toLowerCase();
			}

			rs = metaData.getPrimaryKeys(null, null, tableName);
			while (rs.next()) {
				primaryKeys.add(rs.getString("column_name").toUpperCase());
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
			JdbcUtils.close(connection);
		}
		return primaryKeys;
	}

	public static boolean isAllowedSql(String sql) {
		if (StringUtils.isEmpty(sql)) {
			return false;
		}

		boolean isLegal = true;

		sql = sql.toLowerCase();

		if (sql.contains("userinfo")) {
			isLegal = false;
		}

		if (sql.contains("sys_user")) {
			isLegal = false;
		}
		if (sql.contains("sys_key")) {
			isLegal = false;
		}
		if (sql.contains("sys_server")) {
			isLegal = false;
		}
		if (sql.contains("sys_property")) {
			isLegal = false;
		}
		if (sql.contains("sys_identity_token")) {
			isLegal = false;
		}

		return isLegal;
	}

	public static boolean isAllowedTable(String tableName) {
		if (StringUtils.equalsIgnoreCase(tableName, "USERINFO")) {
			return false;
		} else if (StringUtils.equalsIgnoreCase(tableName, "SYS_USER")) {
			return false;
		} else if (StringUtils.equalsIgnoreCase(tableName, "SYS_DATABASE")) {
			return false;
		} else if (StringUtils.equalsIgnoreCase(tableName, "SYS_SERVER")) {
			return false;
		} else if (StringUtils.equalsIgnoreCase(tableName, "SYS_KEY")) {
			return false;
		} else
			return !StringUtils.equalsIgnoreCase(tableName, "SYS_IDENTITY_TOKEN");
	}

	public static boolean isLegalQuerySql(String sql) {
		if (StringUtils.isEmpty(sql)) {
			return false;
		}

		boolean isLegal = true;

		sql = sql.toLowerCase();
		if (sql.contains(" insert ")) {
			isLegal = false;
		}
		if (sql.contains(" update ")) {
			isLegal = false;
		}
		if (sql.contains(" delete ")) {
			isLegal = false;
		}
		if (sql.contains(" create ")) {
			isLegal = false;
		}
		if (sql.contains(" alter ")) {
			isLegal = false;
		}
		if (sql.contains(" drop ")) {
			isLegal = false;
		}

		return isLegal;
	}

	public static boolean isTableColumn(String columnName) {
		if (columnName == null || columnName.trim().length() < 2 || columnName.trim().length() > 26) {
			return false;
		}
		char[] sourceChrs = columnName.toCharArray();
		char chr = sourceChrs[0];
		if (!((chr == 95) || (65 <= chr && chr <= 90) || (97 <= chr && chr <= 122))) {
			return false;
		}
		for (int i = 1; i < sourceChrs.length; i++) {
			chr = sourceChrs[i];
			if (!((chr == 95) || (47 <= chr && chr <= 57) || (65 <= chr && chr <= 90) || (97 <= chr && chr <= 122))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isTableName(String sourceString) {
		if (sourceString == null || sourceString.trim().length() < 2 || sourceString.trim().length() > 25) {
			return false;
		}
		char[] sourceChrs = sourceString.toCharArray();
		char chr = sourceChrs[0];
		if (!((chr == 95) || (65 <= chr && chr <= 90) || (97 <= chr && chr <= 122))) {
			return false;
		}
		for (int i = 1; i < sourceChrs.length; i++) {
			chr = sourceChrs[i];
			if (!((chr == 95) || (47 <= chr && chr <= 57) || (65 <= chr && chr <= 90) || (97 <= chr && chr <= 122))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isTemoraryTable(String tableName) {
		tableName = tableName.toLowerCase();

		if (tableName.startsWith("tmp_")) {
			return true;
		}
		if (tableName.startsWith("temp_")) {
			return true;
		}
		if (tableName.startsWith("_log_")) {
			return true;
		}
		if (tableName.startsWith("demo_")) {
			return true;
		}
		if (tableName.startsWith("wwv_")) {
			return true;
		}
		if (tableName.startsWith("aq_")) {
			return true;
		}
		if (tableName.startsWith("bsln_")) {
			return true;
		}
		if (tableName.startsWith("mgmt_")) {
			return true;
		}
		if (tableName.startsWith("ogis_")) {
			return true;
		}
		if (tableName.startsWith("ols_")) {
			return true;
		}
		if (tableName.startsWith("em_")) {
			return true;
		}
		if (tableName.startsWith("openls_")) {
			return true;
		}
		if (tableName.startsWith("mrac_")) {
			return true;
		}
		if (tableName.startsWith("orddcm_")) {
			return true;
		}
		if (tableName.startsWith("x_")) {
			return true;
		}
		if (tableName.startsWith("wlm_")) {
			return true;
		}
		if (tableName.startsWith("olap_")) {
			return true;
		}
		if (tableName.startsWith("ggs_")) {
			return true;
		}
		if (tableName.startsWith("logmnrc_")) {
			return true;
		}
		if (tableName.startsWith("logmnrg_")) {
			return true;
		}
		if (tableName.startsWith("sdo_")) {
			return true;
		}
		if (tableName.startsWith("sys_iot_")) {
			return true;
		}
		if (tableName.contains("$")) {
			return true;
		}
		if (tableName.contains("+")) {
			return true;
		}
		if (tableName.contains("-")) {
			return true;
		}
		if (tableName.contains("?")) {
			return true;
		}
		return tableName.contains("=");
	}

	public static String parseSQL(String sql) {
		sql = sql.replaceAll(".*([';]+|(--)+).*", " ");
		sql = StringTools.replace(sql, "$$QUOTE$$", "'");
		return sql;
	}

	public static String removeOrders(String sql) {
		Pattern pattern = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		StringBuffer buf = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(buf, "");
		}
		matcher.appendTail(buf);
		return buf.toString();
	}

	public static boolean renameTable(Connection connection, String sourceTable, String targetTable) {
		if (DBUtils.isAllowedTable(sourceTable) && DBUtils.isAllowedTable(targetTable)) {
			try {
				String sql = "";
				String dbType = DBConnectionFactory.getDatabaseType(connection);
				if ("mysql".equalsIgnoreCase(dbType)) {
					sql = " rename table " + sourceTable + " to " + targetTable;
				} else if ("db2".equalsIgnoreCase(dbType)) {
					sql = " rename table " + sourceTable + " to " + targetTable;
				} else if ("oracle".equalsIgnoreCase(dbType)) {
					sql = " alter table " + sourceTable + " rename to " + targetTable;
				} else if ("postgresql".equalsIgnoreCase(dbType)) {
					sql = " alter table " + sourceTable + " rename to " + targetTable;
				} else if ("sqlserver".equalsIgnoreCase(dbType)) {
					sql = " EXEC sp_rename '" + sourceTable + "', '" + targetTable + "'";
				}
				if (StringUtils.isNotEmpty(sql)) {
					executeSchemaResource(connection, sql);
					return true;
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return false;
	}

	public static boolean renameTable(String sourceTable, String targetTable) {
		Connection connection = null;
		try {
			connection = DBConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			connection.commit();
			return renameTable(connection, sourceTable, targetTable);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(connection);
		}
	}

	public static boolean tableExists(Connection connection, String tableName) {
		DatabaseMetaData dbmd;
		ResultSet rs = null;
		boolean exists = false;
		try {
			dbmd = connection.getMetaData();
			rs = dbmd.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				String table = rs.getString("TABLE_NAME");
				// logger.debug("table:"+table);
				if (StringUtils.equalsIgnoreCase(tableName, table)) {
					exists = true;
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(rs);
		}
		return exists;
	}

	/**
	 * 判断表是否已经存在
	 *
	 * @param tableName 表名称
	 * @return
	 */
	public static boolean tableExists(String tableName) {
		Connection conn = null;
		try {
			conn = DBConnectionFactory.getConnection();
			return !tableExists(conn, tableName);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			JdbcUtils.close(conn);
		}
	}

}