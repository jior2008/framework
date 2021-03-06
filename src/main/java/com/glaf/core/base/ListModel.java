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

import java.util.ArrayList;
import java.util.List;

public class ListModel implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 表后缀
	 */
    private String tableSuffix;

	private List<?> list = new ArrayList<>(200);

	public ListModel() {

	}

	public List<?> getList() {
		return list;
	}

	public String getTableSuffix() {
		return tableSuffix;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public void setTableSuffix(String tableSuffix) {
		this.tableSuffix = tableSuffix;
	}

}
