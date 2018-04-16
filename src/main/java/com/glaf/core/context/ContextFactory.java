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

package com.glaf.core.context;

public final class ContextFactory {

	private static volatile org.springframework.context.ApplicationContext ctx;

	public static org.springframework.context.ApplicationContext getApplicationContext() {
		return ctx;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<?> clazz) {
		if (ctx == null) {
			throw new RuntimeException(" Spring context is null, please check your spring config.");
		}
		String name = clazz.getSimpleName();
		name = name.substring(0, 1).toLowerCase() + name.substring(1);
		return (T) ctx.getBean(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		if (ctx == null) {
			init();
		}
		if (ctx == null) {
			throw new RuntimeException(" Spring context is null, please check your spring config.");
		}
		return (T) ctx.getBean(name);
	}

	public static boolean hasBean(String name) {
		if (ctx == null) {
			init();
		}
		if (ctx == null) {
			throw new RuntimeException(" Spring context is null, please check your spring config.");
		}
		return ctx.containsBean(name);
	}

	protected static void init() {

	}

	public static void setContext(org.springframework.context.ApplicationContext context) {
		if (context != null) {
			ctx = context;
		}
	}

}