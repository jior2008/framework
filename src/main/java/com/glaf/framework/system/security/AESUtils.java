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

package com.glaf.framework.system.security;

import com.glaf.core.context.ContextFactory;
import com.glaf.core.domain.SysKey;
import com.glaf.core.service.SysKeyService;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESUtils {

	/**
	 * 注意key和加密用到的字符串是不一样的 加密还要指定填充的加密模式和填充模式 AES密钥可以是128或者256，加密模式包括ECB, CBC等
	 * ECB模式是分组的模式，CBC是分块加密后，每块与前一块的加密结果异或后再加密 第一块加密的明文是与IV变量进行异或
	 */

	private static final String KEY_ALGORITHM = "AES";

	private static final String ECB_CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";// 分组

	private static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";// 分块

	/**
	 * IV(Initialization Value)是一个初始值，对于CBC模式来说，它必须是随机选取并且需要保密的
	 * 而且它的长度和密码分组相同(比如：对于AES 128为128位，即长度为16的byte类型数组)
	 * 
	 */
	public static final byte[] IVPARAMETERS = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

	private static volatile byte[] DEFAULT_AES_KEY = null;

	static {
		try {
			String provider = "org.bouncycastle.jce.provider.BouncyCastleProvider";
			java.security.Security.addProvider((Provider) Class.forName(provider).newInstance());
		} catch (Exception ignored) {

		}
	}

	/**
	 * 解密数据
	 * 
	 * @param ikey
	 *            密锁串
	 * @param encryptData
	 *            加密的数据
	 * 
	 * @return
	 */
	public static byte[] decode(byte[] ikey, byte[] encryptData) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(KEY_ALGORITHM, "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");// 加解密保持同个随机种子
			random.setSeed(ikey);
			kgen.init(256, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);
			// SecretKeySpec key = getKeySpec(password);
			Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, "BC");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return cipher.doFinal(encryptData);
		} catch (Exception ignored) {

		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param ikey
	 *            解密密钥
	 * @param data
	 *            待解密内容
	 * @return
	 */
	public static byte[] decrypt(String ikey, byte[] data) {
		try {
			SecretKeySpec key = getKeySpec(ikey);
			Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, "BC");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 使用CBC解密
	 * 
	 * @param key
	 * @param ivParameter
	 * @param data
	 *            加密数据
	 * @return 解密后的数据
	 * @throws Exception
	 */
	public static byte[] decryptCBC(byte[] key, byte[] ivParameter, byte[] data) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
		Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);// 初始化
		return cipher.doFinal(data);// 解密
	}

	/**
	 * 使用ECB解密
	 * 
	 * @param key
	 *            密锁
	 * @param data
	 *            加密数据
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptECB(byte[] key, byte[] data) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM, "BC");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}

	/**
	 * 加密数据
	 * 
	 * @param ikey
	 *            密锁串
	 * @param data
	 *            待加密的数据
	 * @return
	 */
	public static byte[] encode(byte[] ikey, byte[] data) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(KEY_ALGORITHM, "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");// 加解密保持同个随机种子
			random.setSeed(ikey);
			kgen.init(256, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);
			// SecretKeySpec key = getKeySpec(password);
			Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, "BC");// 创建密码器
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(data);
		} catch (Exception ignored) {

		}
		return null;
	}

	/**
	 * 加密
	 * 
	 * @param ikey
	 *            加密密码
	 * @param data
	 *            需要加密的内容
	 * @return
	 */
	public static byte[] encrypt(String ikey, byte[] data) {
		try {
			SecretKeySpec key = getKeySpec(ikey);
			Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, "BC");// 创建密码器
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * CBC加密
	 * 
	 * @param key
	 * @param ivParameter
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptCBC(byte[] key, byte[] ivParameter, byte[] data) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
		Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);// 初始化
		return cipher.doFinal(data);// 加密
	}

	/**
	 * ECB加密
	 * 
	 * @param key
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptECB(byte[] key, byte[] data) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM, "BC");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);// 初始化
		return cipher.doFinal(data);// 加密
	}

	/**
	 * Generates a SecretKeySpec for given password
	 *
	 * @param password
	 * @return SecretKeySpec
	 */
	private static SecretKeySpec getKeySpec(String password) {
		// You can change it to 128 if you wish
		int keyLength = 256;
		byte[] keyBytes = new byte[keyLength / 8];
		// explicitly fill with zeros
		Arrays.fill(keyBytes, (byte) 0x0);
		// if password is shorter then key length, it will be zero-padded
		// to key length
		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
		int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
		System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
	}

	protected static SecretKeySpec getKeySpec2(String strKey) {
		byte[] arrayTmp = strKey.getBytes();
		byte[] array = new byte[32]; // 创建一个空的256位即32位字节数组（默认值为0）
		for (int i = 0; i < arrayTmp.length && i < array.length; i++) {
			array[i] = arrayTmp[i];
		}
        return new SecretKeySpec(array, KEY_ALGORITHM);
	}

	public static byte[] initkey() throws Exception {
		// 实例化密钥生成器
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM, "BC");
		kg.init(256);
		// kg.init(128);
		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

	public static byte[] initAndLoadKey() {
		if (DEFAULT_AES_KEY == null) {
			try {
				SysKeyService sysKeyService = ContextFactory.getBean("sysKeyService");
				SysKey sysKey = sysKeyService.getSysKey("AESKey");
				if (sysKey != null && sysKey.getData() != null) {
					DEFAULT_AES_KEY = sysKey.getData();
				}
				if (DEFAULT_AES_KEY == null) {
					// 实例化密钥生成器
					KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM, "BC");
					kg.init(256);
					// kg.init(128);
					SecretKey secretKey = kg.generateKey();
					byte[] bytes = secretKey.getEncoded();
					sysKey = new SysKey();
					sysKey.setId("AESKey");
					sysKey.setCreateBy("system");
					sysKey.setName("AESKey");
					sysKey.setType("AESKey");
					sysKey.setTitle("系统默认的AES密锁");
					sysKey.setData(bytes);
					sysKeyService.save(sysKey);
					DEFAULT_AES_KEY = bytes;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
		return DEFAULT_AES_KEY;
	}

}
