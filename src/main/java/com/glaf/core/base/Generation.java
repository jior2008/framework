/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.glaf.core.base;

import com.glaf.core.model.TableDefinition;
import org.dom4j.Document;

public interface Generation {

	String newline = System.getProperty("line.separator");

	/**
	 * 添加节点
	 * 
	 * @param doc
	 *            文档
	 * @param tableDefinition
	 *            table定义
	 */
	void addNode(Document doc, TableDefinition tableDefinition);
}
