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

package com.glaf.core.security;

import com.glaf.core.util.StringTools;
import com.glaf.core.util.UUID32;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SecurityUtils {

	private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

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
	 *            待解密16进制字符串
	 * @return 解密后的字符串
	 */
	public static String decode(String key, String data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES", "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec("12345678".getBytes()));
			return new String(cipher.doFinal(hex2byte(data.getBytes())));
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * DES算法，加密
	 * 
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @param data
	 *            待加密字符串
	 * @return 加密后的字节流转16进制字符串
	 */
	public static String encode(String key, String data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES", "BC");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec("12345678".getBytes()));
			byte[] bytes = cipher.doFinal(data.getBytes());
			return byte2hex(bytes);
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * 用发送发方公钥加密前面用的加密用对称密钥,形成数字信封
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param symmetryKey
	 *            对称密钥
	 * @param pubKey
	 *            公钥
	 * @return String(经base64编码)
	 */
	public static String generateDigitalEnvelope(SecurityContext ctx, Key symmetryKey, byte[] pubKey) {
		String result;
		InputStream inputStream = null;
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			inputStream = new ByteArrayInputStream(pubKey);
			java.security.cert.Certificate cert = cf.generateCertificate(inputStream);
			inputStream.close();
			PublicKey publicKey = cert.getPublicKey();
			Cipher cipher = Cipher.getInstance(ctx.getAsymmetryAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			result = Base64.encodeBase64String(cipher.doFinal(symmetryKey.getEncoded()));
			return result;
		} catch (Exception ex) {
			throw new SecurityException(ex);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * 生成对称加密用密钥
	 * 
	 * @param ctx
	 *            上下文环境
	 * @return key
	 */
	public static Key generateSecretKey(SecurityContext ctx) {
		try {
			KeyGenerator skg = KeyGenerator.getInstance(ctx.getSymmetryKeyAlgorithm(), ctx.getJceProvider());
			SecureRandom secureRandom = SecureRandom.getInstance(ctx.getSecureRandomAlgorithm());
			skg.init(ctx.getSymmetryKeySize(), secureRandom);
            return skg.generateKey();
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	public static String genKey() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < 31; i++) {
			buffer.append(UUID32.getUUID());
		}
		return buffer.toString();
	}

	public static String genKey2048() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < 64; i++) {
			buffer.append(UUID32.getUUID());
		}
		return buffer.toString();
	}

	/**
	 * 从客户端的keystore得到证书
	 * 
	 * @return X509Certificate 证书
	 */
	public static X509Certificate getCertFromKeystore(InputStream keystoreInputStream, String alias, String password) {
		try {
			X509Certificate x509cert;
			KeyStore ks = KeyStore.getInstance("JKS", "BC");
			ks.load(keystoreInputStream, password.toCharArray());
			x509cert = (X509Certificate) ks.getCertificate(alias);
			return x509cert;
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * 获取24位密锁
	 * 
	 * @param strKey
	 * @return
	 */
	public static byte[] getKeyBytes(String strKey) {
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
	public static byte[] getKeyIvBytes(String strKey) {
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
	 * 从客户端的keystore得到私钥
	 * 
	 * @return key 私钥
	 */
	public static Key getPrivateKeyFromKeystore(InputStream ksInputStream, String password, String alias) {
		try {
			KeyStore ks = KeyStore.getInstance("JKS", "BC");
			ks.load(ksInputStream, password.toCharArray());
            return ks.getKey(alias, password.toCharArray());
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	public static String hash(String plaintext) {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("SHA"); // SHA-1 generator instance
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}

		md.update(plaintext.getBytes(StandardCharsets.UTF_8)); // Message summary
		// generation

		byte[] raw = md.digest(); // Message summary reception
		return new String(Base64.encodeBase64(raw), StandardCharsets.UTF_8);
	}

	private static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0) {
			throw new IllegalArgumentException();
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	/**
	 * 接收方用自己的私钥解开数字信封，得到对称密钥
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param envelope
	 *            数字信封
	 * @param privateKey
	 *            私钥
	 * @return key 对称密钥
	 */
	public static Key openDigitalEnvelope(SecurityContext ctx, String envelope, Key privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(ctx.getAsymmetryAlgorithm(), ctx.getJceProvider());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			envelope = StringTools.replaceIgnoreCase(envelope, " ", "");
			byte[] key = cipher.doFinal(Base64.decodeBase64(envelope));
			SecretKeyFactory skf = SecretKeyFactory.getInstance(ctx.getSymmetryKeyAlgorithm(), ctx.getJceProvider());
			DESKeySpec keySpec = new DESKeySpec(key);
            return skf.generateSecret(keySpec);
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * 用私钥对待签名内容进行签名，形成签名流。
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param content
	 *            待签名内容
	 * @param privateKey
	 *            私钥
	 * @return byte[] 签名流
	 */
	public static byte[] sign(SecurityContext ctx, byte[] content, Key privateKey) {
		try {
			Signature sign = Signature.getInstance(ctx.getSignatureAlgorithm(), ctx.getJceProvider());
			PrivateKey pk = (PrivateKey) privateKey;
			sign.initSign(pk);
			sign.update(content);
            return sign.sign();
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * 进行对称解密
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param cipherContent
	 *            待解密密文。
	 * @param key
	 *            密钥
	 * @return byte[] 解密后明文
	 * 
	 */
	public static byte[] symmetryDecrypt(SecurityContext ctx, byte[] cipherContent, Key key) {
		try {
			byte[] tContent;
			Cipher cipher = Cipher.getInstance(ctx.getSymmetryAlgorithm(), ctx.getJceProvider());
			SecureRandom secureRandom = SecureRandom.getInstance(ctx.getSecureRandomAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key, secureRandom);
			tContent = cipher.doFinal(cipherContent);
			return tContent;
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * 进行对称加密
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param content
	 *            待加密明文。
	 * @param key
	 *            加密密钥
	 * @return byte[] 加密后密文
	 */
	public static byte[] symmetryEncrypt(SecurityContext ctx, byte[] content, Key key) {
		try {
			byte[] cipherContent;
			Cipher cipher = Cipher.getInstance(ctx.getSymmetryAlgorithm(), ctx.getJceProvider());
			SecureRandom secureRandom = SecureRandom.getInstance(ctx.getSecureRandomAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, key, secureRandom);
			cipherContent = cipher.doFinal(content);
			return cipherContent;
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	/**
	 * 公钥验证签名
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param source
	 *            原文
	 * @param signed
	 *            签名信息
	 * @param publicKey
	 *            公钥
	 * @return boolean
	 */
	public static boolean verify(SecurityContext ctx, byte[] source, byte[] signed, PublicKey publicKey) {
		try {
			boolean verify;
			Signature sign = Signature.getInstance(ctx.getSignatureAlgorithm(), ctx.getJceProvider());
			sign.initVerify(publicKey);
			sign.update(source);
			verify = sign.verify(signed);
			return verify;
		} catch (Exception ex) {
			throw new SecurityException(ex);
		}
	}

	private SecurityUtils() {

	}

}