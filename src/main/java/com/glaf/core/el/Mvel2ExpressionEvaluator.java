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

package com.glaf.core.el;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mvel2.MVEL;

import com.glaf.core.util.StringTools;

public class Mvel2ExpressionEvaluator {
	protected final static Log logger = LogFactory.getLog(Mvel2ExpressionEvaluator.class);

	public static Object evaluate(String expression, Map<String, Object> context) {
		Object result = null;
		try {
			if (expression != null && context != null) {
				expression = translateExpression(expression);
				result = MVEL.eval(expression, context);
				if (result != null) {
					if (result instanceof Map<?, ?>) {
						return ((Map<?, ?>) result).keySet().iterator().next();
					}
				}
			}
		} catch (Exception ex) {
			logger.error("evaluate expression error:" + expression);
			throw new RuntimeException(ex);
		}
		return result;
	}

	private static String translateExpression(String expression) {
		if (expression == null) {
			return null;
		}
		expression = StringTools.replaceIgnoreCase(expression, "#{", "");
		expression = StringTools.replaceIgnoreCase(expression, "${", "");
		expression = StringTools.replaceIgnoreCase(expression, "}", "");
		return expression;
	}

}