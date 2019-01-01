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

import org.apache.ibatis.session.SqlSession;

import java.sql.*;
import java.util.List;

public final class JdbcUtils {

	public static void close(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException ignored) {
		}
	}

	public static void close(Connection con, Statement stmt, ResultSet rs) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException ignored) {
		}

		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException ignored) {
		}

		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException ignored) {
		}
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException ignored) {
		}
	}

	public static void close(SqlSession session) {
		try {
			if (session != null) {
				session.close();
			}
		} catch (Exception ignored) {
		}
	}

	public static void close(Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException ignored) {
		}
	}

	public static void close(Statement stmt, ResultSet rs) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException ignored) {
		}

		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException ignored) {
		}
	}

	public static void fillStatement(PreparedStatement stmt, List<Object> params) throws SQLException {
		if (params == null || params.size() == 0) {
			return;
		}
		for (int i = 0, len = params.size(); i < len; i++) {
			Object object = params.get(i);
			if (object != null) {
				if (object instanceof java.sql.Date) {
					java.sql.Date sqlDate = (java.sql.Date) object;
					stmt.setDate(i + 1, sqlDate);
				} else if (object instanceof java.sql.Time) {
					java.sql.Time sqlTime = (java.sql.Time) object;
					stmt.setTime(i + 1, sqlTime);
				} else if (object instanceof java.sql.Timestamp) {
					Timestamp datetime = (Timestamp) object;
					stmt.setTimestamp(i + 1, datetime);
				} else if (object instanceof java.util.Date) {
					Timestamp datetime = DateUtils.toTimestamp((java.util.Date) object);
					stmt.setTimestamp(i + 1, datetime);
				} else {
					stmt.setObject(i + 1, object);
				}
			} else {
				stmt.setString(i + 1, null);
			}
		}
	}

	public static void rollback(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.rollback();
			}
		} catch (SQLException ignored) {
		}
	}

	public static void skipRows(ResultSet rs, int firstResult) throws SQLException {
		if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
			if (firstResult != 0) {
				rs.absolute(firstResult);
			}
		} else {
			for (int i = 0; i < firstResult; i++) {
				rs.next();
			}
		}
	}

	private JdbcUtils() {
	}

}