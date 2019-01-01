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

package com.glaf.core.tree.helper;

import com.glaf.core.base.TreeModel;
import com.glaf.core.tree.component.TreeComponent;
import com.glaf.core.tree.component.TreeRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

class DTreeHelper {

	private final static String newline = System.getProperty("line.separator");

	public DTreeHelper() {

	}

	/**
	 * 输出dtree树的脚本
	 * 
	 * @param components
	 * @return
	 */
	public String buildTreeScript(List<TreeModel> components) {
		return this.buildTreeScript(components, false);
	}

	/**
	 * 输出dtree树的脚本
	 *
	 * @return
	 */
    private String buildTreeScript(List<TreeModel> treeModels, boolean showLink) {
		Collections.sort(treeModels);
		TreeRepositoryBuilder builder = new TreeRepositoryBuilder();
		TreeRepository repository = builder.build(treeModels);
		StringBuilder buffer = new StringBuilder();
		List<?> topComponents = repository.getTopTrees();
        for (Object topComponent : topComponents) {
            TreeComponent component = (TreeComponent) topComponent;
            buffer.append(newline);
            buffer.append("	    ").append("tree").append(".add('")
                    .append(component.getId()).append("', '-1',  '")
                    .append(component.getTitle()).append("'");
            if (showLink) {
                if (StringUtils.isNotEmpty(component.getLocation())) {
                    buffer.append(", \"javascript:gotoLink('")
                            .append(component.getId()).append("','")
                            .append(component.getTitle()).append("','")
                            .append(component.getLocation()).append("');\"");
                } else {
                    buffer.append(", '").append("'");
                }
                if (StringUtils.isNotEmpty(component.getTarget())) {
                    buffer.append(", '").append(component.getTarget())
                            .append("'");
                }
            } else {
                buffer.append(",'',''");
            }
            buffer.append(");");
            buffer.append(newline);
            buffer.append(buildTreeModel(component, "tree", showLink));
            buffer.append(newline);

        }
		buffer.append(newline);
		return buffer.toString();
	}

	private String buildTreeModel(TreeComponent tree, String iTree,
			boolean showLink) {
		StringBuilder buffer = new StringBuilder();
		TreeComponent[] components = tree.getTreeComponents();
        for (TreeComponent component : components) {
            buffer.append("	    ").append(iTree).append(".add('")
                    .append(component.getId()).append("', '")
                    .append(tree.getId()).append("', '")
                    .append(component.getTitle()).append("'");
            if (showLink) {
                if (StringUtils.isNotEmpty(component.getLocation())) {
                    buffer.append(", \"javascript:gotoLink('")
                            .append(component.getId()).append("','")
                            .append(component.getTitle()).append("','")
                            .append(component.getLocation()).append("');\"");
                } else {
                    buffer.append(", '").append("'");
                }
                if (StringUtils.isNotEmpty(component.getTarget())) {
                    buffer.append(", '").append(component.getTarget())
                            .append("'");
                }
            } else {
                buffer.append(",'',''");
            }
            buffer.append(");");
            buffer.append(newline);
        }

        for (TreeComponent component : components) {
            if (component.getComponents() != null
                    && component.getComponents().size() > 0) {
                buffer.append(buildTreeModel(component, iTree, showLink));
            }
        }

		return buffer.toString();
	}

}