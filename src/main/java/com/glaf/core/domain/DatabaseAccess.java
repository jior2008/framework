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
package com.glaf.core.domain;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glaf.core.base.JSONable;
import com.glaf.framework.system.factory.DatabaseAccessJsonFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 
 * 实体对象
 *
 */

@Entity
@Table(name = "SYS_DATABASE_ACCESS")
public class DatabaseAccess implements Serializable, JSONable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID_", nullable = false)
    private Long id;

	@Column(name = "DATABASEID_", nullable = false)
    private Long databaseId;

	@Column(name = "ACTORID_", length = 50, nullable = false)
    private String actorId;

	public DatabaseAccess() {

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatabaseAccess other = (DatabaseAccess) obj;
		if (id == null) {
            return other.id == null;
		} else return id.equals(other.id);
    }

	public String getActorId() {
		return this.actorId;
	}

	public Long getDatabaseId() {
		return this.databaseId;
	}

	public Long getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public DatabaseAccess jsonToObject(JSONObject jsonObject) {
		return DatabaseAccessJsonFactory.jsonToObject(jsonObject);
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public void setDatabaseId(Long databaseId) {
		this.databaseId = databaseId;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JSONObject toJsonObject() {
		return DatabaseAccessJsonFactory.toJsonObject(this);
	}

	public ObjectNode toObjectNode() {
		return DatabaseAccessJsonFactory.toObjectNode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
