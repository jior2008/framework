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

package com.glaf.core.util.serializer;

import org.xerial.snappy.Snappy;

/**
 * FSTSerializer增加snappy
 */
public class FstSnappySerializer implements Serializer {

	private final Serializer inner;

	public FstSnappySerializer() {
		this(new FSTSerializer());
	}

	private FstSnappySerializer(Serializer innerSerializer) {
		this.inner = innerSerializer;
	}

	@Override
	public Object deserialize(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return null;
		try {
			return inner.deserialize(Snappy.uncompress(bytes));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String name() {
		return "fst_snappy";
	}

	@Override
	public byte[] serialize(Object obj) {
		try {
			return Snappy.compress(inner.serialize(obj));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
