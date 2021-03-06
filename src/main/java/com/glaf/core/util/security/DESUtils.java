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

package com.glaf.core.util.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;

class DESUtils {

	private static final String KEY_ALGORITHM = "DES";

	private static final String ECB_CIPHER_ALGORITHM = "DES/ECB/PKCS7Padding";

	private static final String CBC_CIPHER_ALGORITHM = "DES/CBC/PKCS7Padding";

	static {
		try {
			String provider = "org.bouncycastle.jce.provider.BouncyCastleProvider";
			java.security.Security.addProvider((Provider) Class.forName(provider).newInstance());
		} catch (Exception ignored) {

		}
	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b
	 * @return
	 */
	private static String byte2hex(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1) {
				hs.append('0');
			}
			hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}

	/**
	 * DES算法，解密
	 * 
	 * @param key
	 *            解密私钥，长度不能够小于8位
	 * @param data
	 *            待解密数据
	 * @return 解密后的数据
	 */
	private static byte[] decode(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES", "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec("12345678".getBytes()));
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * DES解密
	 * 
	 * @param key
	 *            解密私钥，长度不能够小于8位
	 * @param secretIv
	 *            密锁向量
	 * @param data
	 *            待解密字节流
	 * @return 解密后的数据
	 */
	private static byte[] decode(byte[] key, byte[] secretIv, byte[] data) {
		if (data == null || key == null || secretIv == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(secretIv));
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * DES解密
	 * 
	 * @param key
	 *            解密私钥，长度不能够小于8位
	 * @param data
	 *            待解密字节流
	 * @return 解密后的字符串
	 */
	public static byte[] decode(String key, byte[] data) {
		if (data == null || key == null) {
			return null;
		}
		if (key.length() == 24) {
			return decode(key.getBytes(), data);
		}
		try {
			DESKeySpec dks = new DESKeySpec(getKeyBytes(key));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * DES解密
	 * 
	 * @param key
	 *            解密私钥，长度不能够小于8位
	 * @param secretIv
	 *            密锁向量
	 * @param data
	 *            待解密字节流
	 * @return 解密后的数据
	 */
	public static byte[] decode(String key, String secretIv, byte[] data) {
		if (data == null || key == null || secretIv == null) {
			return null;
		}
		if (key.length() == 24 && secretIv.length() == 8) {
			return decode(key.getBytes(), secretIv.getBytes(), data);
		}
		try {
			DESKeySpec dks = new DESKeySpec(getKeyBytes(key));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(getKeyIvBytes(secretIv)));
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * DES解密
	 * 
	 * @param key
	 *            解密密钥
	 * @param data
	 *            待解密内容
	 * @return
	 */
	public static byte[] decrypt(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom random = new SecureRandom();
			// 创建一个DESKeySpec对象
			DESKeySpec desKey = new DESKeySpec(key);
			// 创建一个密匙工厂
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// 将DESKeySpec对象转换成SecretKey对象
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成解密操作
			Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM, "BC");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			// 真正开始解密操作
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 3DES解密
	 * 
	 * @param input
	 *            需要解密的字节流
	 * @param secretKey
	 *            密钥
	 * @param secretIv
	 *            向量
	 * @return
	 */
	private static byte[] decrypt3DES(byte[] input, byte[] secretKey, byte[] secretIv) {
		if (input == null || secretKey == null || secretIv == null) {
			return null;
		}
		byte[] res;
		try {
			DESedeKeySpec spec = new DESedeKeySpec(secretKey);
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
			Key deskey = keyfactory.generateSecret(spec);
			Cipher cipher = Cipher.getInstance("desede/CBC/PKCS7Padding");
			IvParameterSpec ips = new IvParameterSpec(secretIv);
			cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
			res = cipher.doFinal(input);
			return res;
		} catch (Exception ex) {
			throw new RuntimeException("3DES CBC解密过程出现错误！", ex);
		}
	}

	/**
	 * 3DES解密
	 * 
	 * @param input
	 *            需要解密的字节流
	 * @param secretKey
	 *            密钥
	 * @param secretIv
	 *            向量
	 * @return
	 */
	public static byte[] decrypt3DES(byte[] input, String secretKey, String secretIv) {
		if (input == null || secretKey == null || secretIv == null) {
			return null;
		}
		if (secretKey.length() == 24 && secretIv.length() == 8) {
			return decrypt3DES(input, secretKey.getBytes(), secretIv.getBytes());
		}
		byte[] res;
		try {
			DESedeKeySpec spec = new DESedeKeySpec(getKeyBytes(secretKey));
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
			Key deskey = keyfactory.generateSecret(spec);
			Cipher cipher = Cipher.getInstance("desede/CBC/PKCS7Padding");
			IvParameterSpec ips = new IvParameterSpec(getKeyIvBytes(secretIv));
			cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
			res = cipher.doFinal(input);
			return res;
		} catch (Exception ex) {
			throw new RuntimeException("3DES CBC解密过程出现错误！", ex);
		}
	}

	/**
	 * 3DES CBC加密
	 * 
	 * @param input
	 *            需要加密的数据
	 * @param secretKey
	 *            密钥
	 * @param secretIv
	 *            向量
	 * @return
	 */
	public static byte[] ecrypt3DES(byte[] input, byte[] secretKey, byte[] secretIv) {
		if (input == null || secretKey == null || secretIv == null) {
			return null;
		}
		byte[] res;
		try {
			// 根据给定的字节数组和算法构造一个密钥
			SecretKey deskey = new SecretKeySpec(secretKey, "desede");
			// 加密
			Cipher cipher = Cipher.getInstance("desede/CBC/PKCS7Padding");
			IvParameterSpec ips = new IvParameterSpec(secretIv);
			cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
			res = cipher.doFinal(input);
			return res;
		} catch (Exception ex) {
			throw new RuntimeException("3DES CBC加密过程出现错误！", ex);
		}
	}

	/**
	 * DES算法，加密
	 * 
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @param data
	 *            待加密数据
	 * 
	 * @return 加密后的字符串,16进制字符串
	 */
	public static String encode(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES", "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec("12345678".getBytes()));
			byte[] bytes = cipher.doFinal(data);
			return byte2hex(bytes);
		} catch (Exception ex) {
			throw new RuntimeException("加密过程出现错误！", ex);
		}
	}

	/**
	 * DES算法，加密
	 * 
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @param secretIv
	 *            密锁向量
	 * @param data
	 *            待加密数据
	 * @return 加密后的字节流
	 */
	public static byte[] encode(byte[] key, byte[] secretIv, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(secretIv));
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException("加密过程出现错误！", ex);
		}
	}

	/**
	 * DES算法，加密
	 * 
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @param data
	 *            待加密数据
	 * 
	 * @return 加密后的字节流
	 */
	public static byte[] encode(String key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(getKeyBytes(key));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM, "BC");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException("加密过程出现错误！", ex);
		}
	}

	/**
	 * DES算法，加密
	 * 
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @param secretIv
	 *            密锁向量
	 * @param data
	 *            待加密数据
	 * @return 加密后的字节流
	 */
	public static byte[] encode(String key, String secretIv, byte[] data) {
		if (key == null || secretIv == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(getKeyBytes(key));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(getKeyIvBytes(secretIv)));
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException("加密过程出现错误！", ex);
		}
	}

	/**
	 * DES加密
	 * 
	 * @param key
	 *            加密密钥
	 * @param data
	 *            需要加密的数据
	 * @return
	 */
	public static byte[] encrypt(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(key);
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM, "BC");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM, "BC");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			// 现在，获取数据并加密
			// 正式执行加密操作
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 3DES加密
	 * 
	 * @param key
	 *            加密密钥
	 * @param secretIv
	 *            向量
	 * @param data
	 *            需要加密的数据
	 * @return
	 */
	public static byte[] encrypt3DES(byte[] key, byte[] secretIv, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			SecretKey deskey = new SecretKeySpec(key, "desede");
			Cipher cipher = Cipher.getInstance("desede/CBC/PKCS7Padding");
			IvParameterSpec ips = new IvParameterSpec(secretIv);
			cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
			// 正式执行加密操作
			return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 3DES CBC加密
	 * 
	 * @param secretKey
	 *            密钥
	 * @param secretIv
	 *            向量
	 * @param data
	 *            需要加密的数据
	 * @return
	 */
	public static byte[] encrypt3DES(String secretKey, String secretIv, byte[] data) {
		if (data == null || secretKey == null || secretIv == null) {
			return null;
		}
		try {
			// 根据给定的字节数组和算法构造一个密钥
			// SecretKey deskey = new SecretKeySpec(getKeyBytes(secretKey),
			// "desede");
			SecretKey deskey = getKeySpec(secretKey);
			// 加密
			Cipher cipher = Cipher.getInstance("desede/CBC/PKCS7Padding");
			IvParameterSpec ips = new IvParameterSpec(getKeyIvBytes(secretIv));
			cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
            return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException("3DES CBC加密过程出现错误！", ex);
		}
	}

	/**
	 * 3DES ECB 加密
	 *
	 * @param secretKey
	 *            密钥
	 * @param data
	 *            需要加密的字节流
	 * @return
	 */
	public static byte[] encrypt3DESECB(byte[] secretKey, byte[] data) {
		if (data == null || secretKey == null) {
			return null;
		}
		try {
			DESedeKeySpec spec = new DESedeKeySpec(secretKey);
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
			Key deskey = keyfactory.generateSecret(spec);
			Cipher cipher = Cipher.getInstance("desede/ECB/PKCS7Padding");
			cipher.init(Cipher.DECRYPT_MODE, deskey);
            return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException("3DES ECB加密过程出现错误！", ex);
		}
	}

	/**
	 * 3DES ECB 加密
	 *
	 * @param secretKey
	 *            密钥
	 * @param data
	 *            需要加密的字节流
	 * @return
	 */
	public static byte[] encrypt3DESECB(String secretKey, byte[] data) {
		if (data == null || secretKey == null) {
			return null;
		}
		try {
			DESedeKeySpec spec = new DESedeKeySpec(getKeyBytes(secretKey));
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
			Key deskey = keyfactory.generateSecret(spec);
			Cipher cipher = Cipher.getInstance("desede/ECB/PKCS7Padding");
			cipher.init(Cipher.DECRYPT_MODE, deskey);
            return cipher.doFinal(data);
		} catch (Exception ex) {
			throw new RuntimeException("3DES ECB加密过程出现错误！", ex);
		}
	}

	/**
	 * 获取24位密锁
	 * 
	 * @param strKey
	 * @return
	 */
	private static byte[] getKeyBytes(String strKey) {
		if (null == strKey || strKey.length() < 1) {
			throw new RuntimeException("key is null or empty!");
		}
		java.security.MessageDigest alg;
		try {
			alg = java.security.MessageDigest.getInstance("MD5");
			alg.update(strKey.getBytes());
			byte[] bkey = alg.digest();
			int start = bkey.length;
			byte[] bkey24 = new byte[24];
			for (int i = 0; i < start; i++) {
				bkey24[i] = bkey[i];
			}
			for (int i = start; i < 24; i++) {
				bkey24[i] = bkey[i - start];
			}
			return bkey24;
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 获取8位密锁向量
	 * 
	 * @param strKey
	 * @return
	 */
	private static byte[] getKeyIvBytes(String strKey) {
		if (null == strKey || strKey.length() < 1) {
			throw new RuntimeException("key is null or empty!");
		}
		java.security.MessageDigest alg;
		try {
			alg = java.security.MessageDigest.getInstance("MD5");
			alg.update(strKey.getBytes());
			byte[] bkey = alg.digest();
			int start = bkey.length;
			byte[] bkey8 = new byte[8];
			for (int i = 0; i < start && i < 8; i++) {
				bkey8[i] = bkey[i];
			}
			for (int i = start; i < 8; i++) {
				bkey8[i] = bkey[i - start];
			}
			return bkey8;
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
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
        return new SecretKeySpec(keyBytes, "desede");
	}

	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0) {
			throw new IllegalArgumentException("长度不是偶数");
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	public static byte[] initkey() throws Exception {
		// 实例化密钥生成器
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM, "BC");
		// kg.init(64);
		// kg.init(128);
		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

}
