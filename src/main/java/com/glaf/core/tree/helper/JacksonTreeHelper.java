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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glaf.core.base.TreeModel;
import com.glaf.core.tree.component.TreeComponent;
import com.glaf.core.tree.component.TreeRepository;
import com.glaf.core.util.DateUtils;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class JacksonTreeHelper {
	
	private void addDataMap(TreeComponent component, ObjectNode row) {
		if (component.getDataMap() != null) {
			Map<String, Object> dataMap = component.getDataMap();
			Set<Entry<String, Object>> entrySet = dataMap.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String name = entry.getKey();
				Object value = entry.getValue();
				if (value != null) {
					if (value instanceof Date) {
						Date d = (Date) value;
						row.put(name, DateUtils.getDate(d));
					} else if (value instanceof Boolean) {
						row.put(name, (Boolean) value);
					} else if (value instanceof Integer) {
						row.put(name, (Integer) value);
					} else if (value instanceof Long) {
						row.put(name, (Long) value);
					} else if (value instanceof Double) {
						row.put(name, (Double) value);
					} else if (value instanceof Float) {
						row.put(name, (Float) value);
					} else {
						row.put(name, value.toString());
					}
				}
			}
		}

		if (component.getTreeObject() != null) {
			if (component.getTreeObject() instanceof TreeModel) {
				TreeModel tree = (TreeModel) component.getTreeObject();
				ObjectNode objectNode = tree.toObjectNode();
				if (objectNode != null && !objectNode.isNull()) {
					Iterator<String> iterator = objectNode.fieldNames();
					while (iterator.hasNext()) {
						String nodeName = iterator.next();
						row.set(nodeName, objectNode.get(nodeName));
					}
				}

				row.put("id", tree.getId());
				row.put("_id_", tree.getId());
				row.put("_oid_", tree.getId());
				row.put("parentId", tree.getParentId());
				row.put("_parentId", tree.getParentId());
				row.put("name", tree.getName());
				row.put("text", tree.getName());
				row.put("checked", tree.isChecked());
				row.put("level", tree.getLevel());
				row.put("icon", tree.getIcon());
				row.put("img", tree.getIcon());
				row.put("image", tree.getIcon());
			}
		}
	}

	private TreeRepository build(List<TreeModel> treeModels) {
		Map<String, TreeModel> treeMap = new java.util.HashMap<String, TreeModel>();
		Map<String, TreeModel> lockedMap = new java.util.HashMap<String, TreeModel>();

		for (TreeModel treeModel : treeModels) {
			if (treeModel != null && treeModel.getId() == treeModel.getParentId()) {
				treeModel.setParentId(-1);
			}
			if (treeModel != null && treeModel.getId() > 0) {
				treeMap.put(String.valueOf(treeModel.getId()), treeModel);
			}
			if (treeModel != null && treeModel.getLocked() != 0) {
				/**
				 * 记录已经禁用的节点
				 */
				lockedMap.put(String.valueOf(treeModel.getId()), treeModel);
			}
		}

		for (int i = 0, len = treeModels.size(); i < len / 2; i++) {
			for (TreeModel tree : treeModels) {
				/**
				 * 找到某个节点的父节点，如果被禁用，那么当前节点也设置为禁用
				 */
				if (lockedMap.get(String.valueOf(tree.getParentId())) != null) {
					tree.setLocked(1);
				}
				TreeModel parent = treeMap.get(String.valueOf(tree.getParentId()));
				tree.setParent(parent);
			}
		}

		TreeRepository repository = new TreeRepository();
		for (TreeModel treeModel : treeModels) {
			if (treeModel == null) {
				continue;
			}
			if (treeModel.getLocked() != 0) {
				continue;
			}
			if (treeModel.getParent() != null && treeModel.getParent().getLocked() != 0) {
				continue;
			}
			TreeComponent component = new TreeComponent();
			component.setId(String.valueOf(treeModel.getId()));
			component.setCode(String.valueOf(treeModel.getId()));
			component.setTitle(treeModel.getName());
			component.setChecked(treeModel.isChecked());
			component.setTreeObject(treeModel);
			component.setImage(treeModel.getIcon());
			component.setId(String.valueOf(treeModel.getId()));
			component.setCode(treeModel.getCode());
			component.setTreeModel(treeModel);
			component.setDescription(treeModel.getDescription());
			component.setLocation(treeModel.getUrl());
			component.setUrl(treeModel.getUrl());
			component.setTreeId(treeModel.getTreeId());
			component.setCls(treeModel.getIconCls());
			component.setDataMap(treeModel.getDataMap());
			repository.addTree(component);
		}

		for (TreeModel treeModel : treeModels) {
			if (treeModel == null) {
				continue;
			}
			if (treeModel.getLocked() != 0) {
				continue;
			}
			if (treeModel.getParent() != null && treeModel.getParent().getLocked() != 0) {
				continue;
			}

			TreeComponent component = repository.getTree(String.valueOf(treeModel.getId()));
			String parentId = String.valueOf(treeModel.getParentId());
			if (treeMap.get(parentId) != null) {
				TreeComponent parentTree = repository.getTree(parentId);
				if (parentTree == null) {
					TreeModel parent = treeMap.get(parentId);
					parentTree = new TreeComponent();
					parentTree.setId(String.valueOf(parent.getId()));
					parentTree.setCode(String.valueOf(parent.getId()));
					parentTree.setTitle(parent.getName());
					parentTree.setChecked(parent.isChecked());
					parentTree.setTreeObject(parent);
					parentTree.setImage(parent.getIcon());
					parentTree.setTreeModel(parent);
					parentTree.setDescription(parent.getDescription());
					parentTree.setLocation(parent.getUrl());
					parentTree.setUrl(parent.getUrl());
					parentTree.setTreeId(parent.getTreeId());
					parentTree.setCls(parent.getIconCls());
					parentTree.setDataMap(parent.getDataMap());
					// repository.addTree(parentTree);
				}
				component.setParent(parentTree);
			}
		}
		return repository;
	}

	public TreeRepository buildTree(List<TreeComponent> treeModels) {
		Map<String, TreeComponent> treeMap = new java.util.HashMap<String, TreeComponent>();
		Map<String, TreeComponent> lockedMap = new java.util.HashMap<String, TreeComponent>();

		for (TreeComponent treeModel : treeModels) {
			if (treeModel != null && StringUtils.equals(treeModel.getId(), treeModel.getParentId())) {
				treeModel.setParentId("-1");
			}
			if (treeModel != null && treeModel.getId() != null) {
				treeMap.put(treeModel.getId(), treeModel);
			}
			if (treeModel != null && treeModel.getLocked() != 0) {
				/**
				 * 记录已经禁用的节点
				 */
				lockedMap.put(treeModel.getId(), treeModel);
			}
		}

		for (int i = 0, len = treeModels.size(); i < len / 2; i++) {
			for (TreeComponent tree : treeModels) {
				/**
				 * 找到某个节点的父节点，如果被禁用，那么当前节点也设置为禁用
				 */
				if (lockedMap.get(String.valueOf(tree.getParentId())) != null) {
					tree.setLocked(1);
				}
				TreeComponent parent = treeMap.get(String.valueOf(tree.getParentId()));
				tree.setParent(parent);
			}
		}

		TreeRepository repository = new TreeRepository();
		for (TreeComponent treeModel : treeModels) {
			if (treeModel == null) {
				continue;
			}
			if (treeModel.getLocked() != 0) {
				continue;
			}
			if (treeModel.getParent() != null && treeModel.getParent().getLocked() != 0) {
				continue;
			}
			TreeComponent component = new TreeComponent();
			component.setId(String.valueOf(treeModel.getId()));
			component.setCode(String.valueOf(treeModel.getId()));
			component.setTitle(treeModel.getTitle());
			component.setChecked(treeModel.isChecked());
			component.setTreeObject(treeModel);
			component.setImage(treeModel.getImage());
			component.setId(String.valueOf(treeModel.getId()));
			component.setCode(treeModel.getCode());
			// component.setTreeModel(treeModel);
			component.setDescription(treeModel.getDescription());
			component.setLocation(treeModel.getUrl());
			component.setUrl(treeModel.getUrl());
			component.setTreeId(treeModel.getTreeId());
			component.setCls(treeModel.getCls());
			component.setDataMap(treeModel.getDataMap());
			repository.addTree(component);
		}

		for (TreeComponent treeModel : treeModels) {
			if (treeModel == null) {
				continue;
			}
			if (treeModel.getLocked() != 0) {
				continue;
			}
			if (treeModel.getParent() != null && treeModel.getParent().getLocked() != 0) {
				continue;
			}

			TreeComponent component = repository.getTree(String.valueOf(treeModel.getId()));
			String parentId = String.valueOf(treeModel.getParentId());
			if (treeMap.get(parentId) != null) {
				TreeComponent parentTree = repository.getTree(String.valueOf(parentId));
				if (parentTree == null) {
					TreeComponent parent = treeMap.get(parentId);
					parentTree = new TreeComponent();
					parentTree.setId(String.valueOf(parent.getId()));
					parentTree.setCode(String.valueOf(parent.getId()));
					parentTree.setTitle(parent.getTitle());
					parentTree.setChecked(parent.isChecked());
					parentTree.setTreeObject(parent);
					parentTree.setImage(parent.getImage());
					// parentTree.setTreeModel(parent);
					parentTree.setDescription(parent.getDescription());
					parentTree.setLocation(parent.getUrl());
					parentTree.setUrl(parent.getUrl());
					parentTree.setTreeId(parent.getTreeId());
					parentTree.setCls(parent.getCls());
					parentTree.setDataMap(parent.getDataMap());
					// repository.addTree(parentTree);
				}
				component.setParent(parentTree);
			}
		}
		return repository;
	}

	private void buildTree(ObjectNode row, TreeComponent treeComponent, Collection<String> checkedNodes,
						   Map<String, TreeModel> nodeMap) {
		if (treeComponent.getComponents() != null && treeComponent.getComponents().size() > 0) {
			ArrayNode array = new ObjectMapper().createArrayNode();
			for (TreeComponent component : treeComponent.getComponents()) {
				ObjectNode child = new ObjectMapper().createObjectNode();
				this.addDataMap(component, child);
				child.put("id", component.getId());
				child.put("code", component.getCode());
				child.put("name", component.getTitle());
				child.put("text", component.getTitle());
				child.put("checked", component.isChecked());
				child.put("icon", component.getImage());
				child.put("img", component.getImage());
				child.put("image", component.getImage());

				if (checkedNodes.contains(component.getId())) {
					child.put("checked", Boolean.valueOf(true));
				} else {
					child.put("checked", Boolean.valueOf(false));
				}
				if (component.getComponents() != null && component.getComponents().size() > 0) {
					child.put("leaf", Boolean.valueOf(false));
					child.put("isParent", true);
				} else {
					child.put("leaf", Boolean.valueOf(true));
					child.put("isParent", false);
				}
				array.add(child);
				this.buildTree(child, component, checkedNodes, nodeMap);
			}
			row.set("children", array);
		}

	}

	private void buildTreeModel(ObjectNode row, TreeComponent treeComponent) {
		if (treeComponent.getComponents() != null && treeComponent.getComponents().size() > 0) {
			ArrayNode array = new ObjectMapper().createArrayNode();
			for (TreeComponent component : treeComponent.getComponents()) {
				ObjectNode child = new ObjectMapper().createObjectNode();
				this.addDataMap(component, child);
				child.put("id", component.getId());
				child.put("code", component.getCode());
				child.put("text", component.getTitle());
				child.put("name", component.getTitle());
				child.put("checked", component.isChecked());
				child.put("icon", component.getImage());
				child.put("img", component.getImage());
				child.put("image", component.getImage());

				if (component.getComponents() != null && component.getComponents().size() > 0) {
					child.put("leaf", Boolean.valueOf(false));
					child.put("isParent", true);
					array.add(child);
					this.buildTreeModel(child, component);
				} else {
					child.put("leaf", Boolean.valueOf(true));
					child.put("isParent", false);
					array.add(child);
				}
			}
			row.set("children", array);
		}
	}

	public ObjectNode getJsonCheckboxNode(TreeModel root, List<TreeModel> trees, List<TreeModel> selectedNodes) {
		Collection<String> checkedNodes = new HashSet<String>();
		if (selectedNodes != null && selectedNodes.size() > 0) {
			for (TreeModel selectedNode : selectedNodes) {
				checkedNodes.add(String.valueOf(selectedNode.getId()));
			}
		}

		Map<String, TreeModel> nodeMap = new java.util.HashMap<String, TreeModel>();
		if (trees != null && trees.size() > 0) {
			for (TreeModel tree : trees) {
				nodeMap.put(String.valueOf(tree.getId()), tree);
			}
		}

		ObjectNode object = new ObjectMapper().createObjectNode();

		if (root != null) {
			object.put("id", String.valueOf(root.getId()));
			object.put("code", String.valueOf(root.getId()));
			object.put("name", root.getName());
			object.put("text", root.getName());
			object.put("leaf", Boolean.valueOf(false));
			object.put("isParent", true);
			object.put("checked", root.isChecked());
			object.put("icon", root.getIcon());
			object.put("img", root.getIcon());
			object.put("image", root.getIcon());

			if (checkedNodes.contains(String.valueOf(root.getId()))) {
				object.put("checked", Boolean.valueOf(true));
			} else {
				object.put("checked", Boolean.valueOf(false));
			}
		}

		ArrayNode array = new ObjectMapper().createArrayNode();
		if (trees != null && trees.size() > 0) {
			TreeRepository repository = this.build(trees);
			if (repository != null) {
				List<?> topTrees = repository.getTopTrees();
				if (topTrees != null && topTrees.size() > 0) {
					if (topTrees.size() == 1) {
						TreeComponent component = (TreeComponent) topTrees.get(0);
						assert root != null;
						if (StringUtils.equals(component.getId(), String.valueOf(root.getId()))) {
							this.buildTree(object, component, checkedNodes, nodeMap);
						} else {
							ObjectNode child = new ObjectMapper().createObjectNode();
							this.addDataMap(component, child);
							child.put("id", component.getId());
							child.put("code", component.getCode());
							child.put("name", component.getTitle());
							child.put("text", component.getTitle());
							child.put("leaf", Boolean.valueOf(false));
							child.put("isParent", true);
							child.put("checked", component.isChecked());
							child.put("icon", component.getImage());
							child.put("img", component.getImage());
							child.put("image", component.getImage());

							if (checkedNodes.contains(component.getId())) {
								child.put("checked", Boolean.valueOf(true));
							} else {
								child.put("checked", Boolean.valueOf(false));
							}
							array.add(child);
							object.set("children", array);
							this.buildTree(child, component, checkedNodes, nodeMap);
						}
					} else {
						for (Object topTree : topTrees) {
							TreeComponent component = (TreeComponent) topTree;
							ObjectNode child = new ObjectMapper().createObjectNode();
							this.addDataMap(component, child);
							child.put("id", component.getId());
							child.put("code", component.getCode());
							child.put("name", component.getTitle());
							child.put("text", component.getTitle());
							child.put("checked", component.isChecked());
							child.put("icon", component.getImage());
							child.put("img", component.getImage());
							child.put("image", component.getImage());

							if (checkedNodes.contains(component.getId())) {
								child.put("checked", Boolean.valueOf(true));
							} else {
								child.put("checked", Boolean.valueOf(false));
							}
							if (component.getComponents() != null && component.getComponents().size() > 0) {
								child.put("leaf", Boolean.valueOf(false));
								child.put("isParent", true);
							} else {
								child.put("leaf", Boolean.valueOf(true));
								child.put("isParent", false);
							}
							array.add(child);
							this.buildTree(child, component, checkedNodes, nodeMap);
						}
						object.set("children", array);
					}
				}
			}
		}

		return object;
	}

	public ArrayNode getTreeArrayNode(List<TreeModel> treeModels) {
		return this.getTreeArrayNode(treeModels, true);
	}

	private ArrayNode getTreeArrayNode(List<TreeModel> treeModels, boolean showParentIfNotChildren) {
		ArrayNode result = new ObjectMapper().createArrayNode();
		if (treeModels != null && treeModels.size() > 0) {
			TreeRepository repository = this.build(treeModels);
			List<?> topTrees = repository.getTopTrees();
			//logger.debug("topTrees:" + (topTrees != null ? topTrees.size() : 0));
			if (topTrees != null && topTrees.size() > 0) {
				for (Object topTree : topTrees) {
					TreeComponent component = (TreeComponent) topTree;
					ObjectNode child = new ObjectMapper().createObjectNode();
					this.addDataMap(component, child);

					child.put("id", component.getId());
					child.put("code", component.getCode());
					child.put("text", component.getTitle());
					child.put("name", component.getTitle());
					child.put("checked", component.isChecked());
					child.put("icon", component.getImage());
					child.put("img", component.getImage());
					child.put("image", component.getImage());

					if (component.getComponents() != null && component.getComponents().size() > 0) {
						child.put("leaf", Boolean.valueOf(false));
						child.put("cls", "folder");
						child.put("isParent", true);
						child.put("classes", "folder");
						result.add(child);
						this.buildTreeModel(child, component);
					} else {
						child.put("leaf", Boolean.valueOf(true));
						child.put("isParent", false);
						if (showParentIfNotChildren) {
							result.add(child);
						}
					}
				}
			}
		}

		return result;
	}

	public ObjectNode getTreeJson(List<TreeModel> treeModels) {
		return this.getTreeJson(null, treeModels);
	}

	public ObjectNode getTreeJson(List<TreeModel> treeModels, boolean showParentIfNotChildren) {
		return this.getTreeJson(null, treeModels, showParentIfNotChildren);
	}

	private ObjectNode getTreeJson(TreeModel root, List<TreeModel> treeModels) {
		return this.getTreeJson(root, treeModels, true);
	}

	private ObjectNode getTreeJson(TreeModel root, List<TreeModel> treeModels, boolean showParentIfNotChildren) {
		ObjectNode object = new ObjectMapper().createObjectNode();
		if (root != null) {
			object = root.toObjectNode();
			object.put("id", String.valueOf(root.getId()));
			object.put("code", String.valueOf(root.getId()));
			object.put("name", root.getName());
			object.put("text", root.getName());
			object.put("leaf", Boolean.valueOf(false));
			object.put("cls", "folder");
			object.put("isParent", true);
			object.put("checked", root.isChecked());
			object.put("icon", root.getIcon());
			object.put("img", root.getIcon());
			object.put("image", root.getIcon());
		}

		if (treeModels != null && treeModels.size() > 0) {
			ArrayNode array = new ObjectMapper().createArrayNode();
			TreeRepository repository = this.build(treeModels);
			if (repository != null) {
				List<?> topTrees = repository.getTopTrees();
				if (topTrees != null && topTrees.size() > 0) {
					if (topTrees.size() == 1) {
						TreeComponent component = (TreeComponent) topTrees.get(0);
						if (root != null) {
							if (StringUtils.equals(component.getId(), String.valueOf(root.getId()))) {
								this.buildTreeModel(object, component);
							}
						} else {
							this.addDataMap(component, object);
							object.put("id", component.getId());
							object.put("code", component.getCode());
							object.put("name", component.getTitle());
							object.put("text", component.getTitle());
							object.put("leaf", Boolean.valueOf(false));
							object.put("cls", "folder");
							object.put("checked", component.isChecked());
							object.put("isParent", true);
							this.buildTreeModel(object, component);
						}
					} else {
						for (Object topTree : topTrees) {
							TreeComponent component = (TreeComponent) topTree;
							ObjectNode child = new ObjectMapper().createObjectNode();
							this.addDataMap(component, child);
							child.put("id", component.getId());
							child.put("code", component.getCode());
							child.put("name", component.getTitle());
							child.put("text", component.getTitle());
							child.put("checked", component.isChecked());
							child.put("icon", component.getImage());
							child.put("img", component.getImage());
							child.put("image", component.getImage());

							if (component.getComponents() != null && component.getComponents().size() > 0) {
								child.put("leaf", Boolean.valueOf(false));
								child.put("cls", "folder");
								child.put("isParent", true);
								array.add(child);
								this.buildTreeModel(child, component);
							} else {
								if (showParentIfNotChildren) {
									child.put("leaf", Boolean.valueOf(true));
									child.put("isParent", false);
									array.add(child);
								}
							}
						}
						object.set("children", array);
					}
				}
			}
		}

		return object;
	}

}
