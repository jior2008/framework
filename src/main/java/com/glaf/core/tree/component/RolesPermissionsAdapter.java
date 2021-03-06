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

package com.glaf.core.tree.component;

import java.util.regex.Pattern;

public class RolesPermissionsAdapter implements PermissionsAdapter {
	protected final Pattern delimiters = Pattern.compile("(?<!\\\\),");

	/**
	 * If the menu is allowed, this should return true.
	 * 
	 * @return whether or not the menu is allowed.
	 */
	public boolean isAllowed(TreeComponent menu) {
		if (menu.getRoles() == null) {
			return true; // no roles define, allow everyone
		}
		return false;
	}

}