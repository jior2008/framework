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

package com.glaf.core.base;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glaf.core.factory.ConnectionDefinitionJsonFactory;


public class ConnectionDefinition implements java.io.Serializable, JSONable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String provider;

	private String type;

	private String name;

	private String subject;

	private String datasource;

	private String database;

	private String host;

	private int port;

	private String driver;

	private String url;

	private String user;

	private String password;

	private String attribute;

	private boolean autoCommit;

	private java.util.Properties properties;

	public ConnectionDefinition() {

	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionDefinition other = (ConnectionDefinition) obj;
		if (name == null) {
            return other.name == null;
		} else return name.equals(other.name);
    }

	public String getAttribute() {
		return attribute;
	}

	public String getDatabase() {
		return database;
	}

	public String getDatasource() {
		return datasource;
	}

	public String getDriver() {
		return driver;
	}

	public String getHost() {
		return host;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public java.util.Properties getProperties() {
		return properties;
	}

	public String getProvider() {
		return provider;
	}

	public String getSubject() {
		return subject;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public ConnectionDefinition jsonToObject(JSONObject jsonObject) {
		return ConnectionDefinitionJsonFactory.jsonToObject(jsonObject);
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setProperties(java.util.Properties properties) {
		this.properties = properties;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public JSONObject toJsonObject() {
		return ConnectionDefinitionJsonFactory.toJsonObject(this);
	}

	public ObjectNode toObjectNode() {
		return ConnectionDefinitionJsonFactory.toObjectNode(this);
	}

	public String toString() {
		return "ConnectionDefinition [name=" + name + ", driver=" + driver
				+ ", url=" + url + ", user=" + user + "]";
	}

}
