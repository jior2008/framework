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

import com.glaf.core.model.ColumnDefinition;
import com.glaf.core.model.TableDefinition;
import com.glaf.core.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BulkInsertBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 批量插入数据
     */
    public void bulkInsert(Connection conn, TableDefinition tableDefinition,
                           List<Map<String, Object>> dataList) {
        List<ColumnDefinition> columns = tableDefinition.getColumns();
        if (columns == null || columns.isEmpty()) {
            throw new RuntimeException("table columns is null");
        }
        String dbType = DBConnectionFactory.getDatabaseType(conn);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int len = columns.size();
        int batchSize = 1000 / len;
        for (Map<String, Object> stringObjectMap : dataList) {
            list.add(stringObjectMap);
            if (list.size() > 0 && list.size() % batchSize == 0) {
                if (StringUtils.equals(dbType, DBUtils.ORACLE)) {
                    this.insertInner(conn, tableDefinition, list);
                } else {
                    this.bulkInsertInner(conn, tableDefinition, list);
                }
                list.clear();
            }
        }
        if (list.size() > 0) {
            if (StringUtils.equals(dbType, DBUtils.ORACLE)) {
                this.insertInner(conn, tableDefinition, list);
            } else {
                this.bulkInsertInner(conn, tableDefinition, list);
            }
            list.clear();
        }
    }

    private void bulkInsertInner(Connection conn, TableDefinition tableDefinition,
                                 List<Map<String, Object>> dataList) {
        List<ColumnDefinition> columns = tableDefinition.getColumns();
        StringBuilder insertBuffer = new StringBuilder(500);
        insertBuffer.append(" insert into ").append(tableDefinition.getTableName()).append(" (");
        for (ColumnDefinition column : columns) {
            insertBuffer.append(column.getColumnName()).append(", ");
        }
        insertBuffer.delete(insertBuffer.length() - 2, insertBuffer.length());
        insertBuffer.append(" ) values ");
        for (int k = 0, l = dataList.size(); k < l; k++) {
            if (k > 0) {
                insertBuffer.append(", ");
            }
            insertBuffer.append(" ( ");
            for (int i = 0, len = columns.size(); i < len; i++) {
                insertBuffer.append("? ");
                if (i < len - 1) {
                    insertBuffer.append(",");
                }
            }
            insertBuffer.append(" ) ");
        }
        int index = 1;
        String columnName;
        String javaType;
        Map<String, Object> rowMap;
        LowerLinkedMap dataMap;
        PreparedStatement psmt = null;
        ByteArrayInputStream bais = null;
        BufferedInputStream bis = null;
        InputStream is = null;
        try {
            String dbType = DBConnectionFactory.getDatabaseType(conn);
            psmt = conn.prepareStatement(insertBuffer.toString());
            for (Map<String, Object> stringObjectMap : dataList) {
                // Map<String, Object> dataMap = dataList.get(k);
                rowMap = stringObjectMap;
                dataMap = new LowerLinkedMap();
                dataMap.putAll(rowMap);
                // logger.debug("dataMap:" + dataMap);
                for (ColumnDefinition column : columns) {
                    columnName = column.getColumnName().toLowerCase();
                    javaType = column.getJavaType();
                    switch (javaType) {
                        case "Integer":
                            if (dataMap.get(columnName) != null) {
                                psmt.setInt(index++, ParamUtils.getInt(dataMap, columnName));
                            } else {
                                psmt.setInt(index++, Types.NULL);
                            }
                            break;
                        case "Long":
                            if (dataMap.get(columnName) != null) {
                                psmt.setLong(index++, ParamUtils.getLong(dataMap, columnName));
                            } else {
                                psmt.setLong(index++, Types.NULL);
                            }
                            break;
                        case "Double":
                            if (dataMap.get(columnName) != null) {
                                psmt.setDouble(index++, ParamUtils.getDouble(dataMap, columnName));
                            } else {
                                psmt.setDouble(index++, Types.NULL);
                            }
                            break;
                        case "Date":
                            Timestamp t = ParamUtils.getTimestamp(dataMap, columnName);
                            if (t != null) {
                                psmt.setTimestamp(index++, t);
                            } else {
                                psmt.setNull(index++, Types.TIMESTAMP);
                            }
                            break;
                        case "String":
                            psmt.setString(index++, ParamUtils.getString(dataMap, columnName));
                            break;
                        case "Clob":
                            String text = ParamUtils.getString(dataMap, columnName);
                            if (text != null) {
                                if ("oracle".equals(dbType)) {
                                    Reader clobReader = new StringReader(text);
                                    psmt.setCharacterStream(index++, clobReader, text.length());
                                } else {
                                    psmt.setString(index++, text);
                                }
                            } else {
                                if ("oracle".equals(dbType)) {
                                    psmt.setCharacterStream(index++, null);
                                } else {
                                    psmt.setString(index++, null);
                                }
                            }
                            break;
                        case "Blob":
                            int index2 = index++;
                            Object object = dataMap.get(columnName);
                            if (object != null) {
                                if (object instanceof byte[]) {
                                    byte[] data = (byte[]) object;
                                    if (data.length > 0) {
                                        try {
                                            bais = new ByteArrayInputStream(data);
                                            bis = new BufferedInputStream(bais);
                                            psmt.setBinaryStream(index2, bis);
                                        } catch (SQLException ex) {
                                            psmt.setBytes(index2, data);
                                        } finally {
                                            IOUtils.closeQuietly(bais);
                                            IOUtils.closeQuietly(bis);
                                        }
                                    }
                                } else if (object instanceof Blob) {
                                    Blob blob = (Blob) object;
                                    is = blob.getBinaryStream();
                                    byte[] data = FileUtils.getBytes(is);
                                    if (data != null && data.length > 0) {
                                        try {
                                            bais = new ByteArrayInputStream(data);
                                            bis = new BufferedInputStream(bais);
                                            psmt.setBinaryStream(index2, bis);
                                        } catch (SQLException ex) {
                                            psmt.setBytes(index2, data);
                                        } finally {
                                            IOUtils.closeQuietly(bais);
                                            IOUtils.closeQuietly(bis);
                                        }
                                    }
                                    IOUtils.closeQuietly(is);
                                } else if (object instanceof InputStream) {
                                    is = (InputStream) object;
                                    byte[] data = FileUtils.getBytes(is);
                                    if (data != null && data.length > 0) {
                                        try {
                                            bais = new ByteArrayInputStream(data);
                                            bis = new BufferedInputStream(bais);
                                            psmt.setBinaryStream(index2, bis);
                                        } catch (SQLException ex) {
                                            psmt.setBytes(index2, data);
                                        } finally {
                                            IOUtils.closeQuietly(bais);
                                            IOUtils.closeQuietly(bis);
                                        }
                                    }
                                    IOUtils.closeQuietly(is);
                                }
                            } else {
                                psmt.setNull(index2, Types.NULL);
                            }
                            break;
                        default:
                            psmt.setObject(index++, ParamUtils.getObject(dataMap, columnName));
                            break;
                    }
                }
            }
            psmt.executeUpdate();
            psmt.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(bis);
            JdbcUtils.close(psmt);
        }
    }

    private void insertInner(Connection conn, TableDefinition tableDefinition, List<Map<String, Object>> dataList) {
        List<ColumnDefinition> columns = tableDefinition.getColumns();
        StringBuilder insertBuffer = new StringBuilder(500);

        insertBuffer.append(" insert into ").append(tableDefinition.getTableName()).append(" (");
        for (ColumnDefinition column : columns) {
            insertBuffer.append(column.getColumnName()).append(", ");
        }
        insertBuffer.delete(insertBuffer.length() - 2, insertBuffer.length());
        insertBuffer.append(" ) values ( ");
        for (int k = 0, l = columns.size(); k < l; k++) {
            insertBuffer.append("? ");
            if (k < l - 1) {
                insertBuffer.append(", ");
            }
        }
        insertBuffer.append(" ) ");

        logger.debug(insertBuffer.toString());
        int index;
        String columnName;
        String javaType;
        Map<String, Object> rowMap;
        LowerLinkedMap dataMap;
        PreparedStatement psmt = null;
        ByteArrayInputStream bais = null;
        BufferedInputStream bis = null;
        InputStream is = null;
        try {
            String dbType = DBConnectionFactory.getDatabaseType(conn);
            psmt = conn.prepareStatement(insertBuffer.toString());
            for (Map<String, Object> stringObjectMap : dataList) {
                index = 1;
                // Map<String, Object> dataMap = dataList.get(k);
                rowMap = stringObjectMap;
                dataMap = new LowerLinkedMap();
                dataMap.putAll(rowMap);
                // logger.debug("dataMap:" + dataMap);
                for (ColumnDefinition column : columns) {
                    columnName = column.getColumnName().toLowerCase();
                    javaType = column.getJavaType();
                    switch (javaType) {
                        case "Integer":
                            if (dataMap.get(columnName) != null) {
                                psmt.setInt(index++, ParamUtils.getInt(dataMap, columnName));
                            } else {
                                psmt.setInt(index++, Types.NULL);
                            }
                            break;
                        case "Long":
                            if (dataMap.get(columnName) != null) {
                                psmt.setLong(index++, ParamUtils.getLong(dataMap, columnName));
                            } else {
                                psmt.setLong(index++, Types.NULL);
                            }
                            break;
                        case "Double":
                            if (dataMap.get(columnName) != null) {
                                psmt.setDouble(index++, ParamUtils.getDouble(dataMap, columnName));
                            } else {
                                psmt.setDouble(index++, Types.NULL);
                            }
                            break;
                        case "Date":
                            Timestamp t = ParamUtils.getTimestamp(dataMap, columnName);
                            if (t != null) {
                                psmt.setTimestamp(index++, t);
                            } else {
                                psmt.setNull(index++, Types.TIMESTAMP);
                            }
                            break;
                        case "String":
                            psmt.setString(index++, ParamUtils.getString(dataMap, columnName));
                            break;
                        case "Clob":
                            String text = ParamUtils.getString(dataMap, columnName);
                            if (text != null) {
                                if ("oracle".equals(dbType)) {
                                    Reader clobReader = new StringReader(text);
                                    psmt.setCharacterStream(index++, clobReader, text.length());
                                } else {
                                    psmt.setString(index++, text);
                                }
                            } else {
                                if ("oracle".equals(dbType)) {
                                    psmt.setCharacterStream(index++, null);
                                } else {
                                    psmt.setString(index++, null);
                                }
                            }
                            break;
                        case "Blob":
                            int index2 = index++;
                            Object object = dataMap.get(columnName);
                            if (object != null) {
                                if (object instanceof byte[]) {
                                    byte[] data = (byte[]) object;
                                    if (data.length > 0) {
                                        try {
                                            bais = new ByteArrayInputStream(data);
                                            bis = new BufferedInputStream(bais);
                                            psmt.setBinaryStream(index2, bis);
                                        } catch (SQLException ex) {
                                            psmt.setBytes(index2, data);
                                        } finally {
                                            IOUtils.closeQuietly(bais);
                                            IOUtils.closeQuietly(bis);
                                        }
                                    }
                                } else if (object instanceof Blob) {
                                    Blob blob = (Blob) object;
                                    is = blob.getBinaryStream();
                                    byte[] data = FileUtils.getBytes(is);
                                    if (data.length > 0) {
                                        try {
                                            bais = new ByteArrayInputStream(data);
                                            bis = new BufferedInputStream(bais);
                                            psmt.setBinaryStream(index2, bis);
                                        } catch (SQLException ex) {
                                            psmt.setBytes(index2, data);
                                        } finally {
                                            IOUtils.closeQuietly(bais);
                                            IOUtils.closeQuietly(bis);
                                        }
                                    }
                                    IOUtils.closeQuietly(is);
                                } else if (object instanceof InputStream) {
                                    is = (InputStream) object;
                                    byte[] data = FileUtils.getBytes(is);
                                    if (data.length > 0) {
                                        try {
                                            bais = new ByteArrayInputStream(data);
                                            bis = new BufferedInputStream(bais);
                                            psmt.setBinaryStream(index2, bis);
                                        } catch (SQLException ex) {
                                            psmt.setBytes(index2, data);
                                        } finally {
                                            IOUtils.closeQuietly(bais);
                                            IOUtils.closeQuietly(bis);
                                        }
                                    }
                                    IOUtils.closeQuietly(is);
                                }
                            } else {
                                psmt.setNull(index2, Types.NULL);
                            }
                            break;
                        default:
                            psmt.setObject(index++, ParamUtils.getObject(dataMap, columnName));
                            break;
                    }
                }
                psmt.addBatch();
            }
            psmt.executeBatch();
            psmt.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(bis);
            JdbcUtils.close(psmt);
        }
    }

}
