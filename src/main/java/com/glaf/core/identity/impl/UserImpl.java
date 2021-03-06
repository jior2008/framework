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

package com.glaf.core.identity.impl;

import com.alibaba.fastjson.JSONObject;
import com.glaf.core.identity.User;
import com.glaf.core.identity.util.UserJsonFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class UserImpl implements User {

	private static final long serialVersionUID = 1L;

	protected int accountType;

	protected String activationCode;

	protected String actorId;

	protected String adminFlag;

	protected String computerId;

	protected Date createDate;

	protected long organizationId;

	protected int domainIndex;

	protected int dumpflag;

	protected String fax;

	protected long id;

	protected String isBind;

	protected Date lastLoginDate;

	protected int locked;

	protected String loginIP;

	protected int loginRetry;

	protected String mail;

	protected String mailPwd;

	protected String mailUser;

	protected String mobile;

	protected String name;

	protected String passwordHash;

	protected String phoneNumber;

	protected String principalshipCode;

	protected String tenantId;

	protected String remark;

	protected Integer remoteAttr;

	protected String superiorId;

	protected int userType;

	protected String token;

	protected Collection<String> rowKeys = new HashSet<String>();

	public UserImpl() {

	}

	public void addRowKey(String rowKey) {
		if (rowKeys == null) {
			rowKeys = new HashSet<String>();
		}
		rowKeys.add(rowKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserImpl other = (UserImpl) obj;
		if (actorId == null) {
            return other.actorId == null;
		} else return actorId.equals(other.actorId);
    }

	public int getAccountType() {
		return accountType;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public String getActorId() {
		return actorId;
	}

	public String getAdminFlag() {
		return adminFlag;
	}

	public String getComputerId() {
		return computerId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public int getDomainIndex() {
		return domainIndex;
	}

	public int getDumpflag() {
		return dumpflag;
	}

	public String getFax() {
		return fax;
	}

	public long getId() {
		return id;
	}

	public String getIsBind() {
		return isBind;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public int getLocked() {
		return locked;
	}

	public String getLoginIP() {
		return loginIP;
	}

	public int getLoginRetry() {
		return loginRetry;
	}

	public String getMail() {
		return mail;
	}

	public String getMailPwd() {
		return mailPwd;
	}

	public String getMailUser() {
		return mailUser;
	}

	public String getMobile() {
		return mobile;
	}

	public String getName() {
		return name;
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getPrincipalshipCode() {
		return principalshipCode;
	}

	public String getRemark() {
		return remark;
	}

	public Integer getRemoteAttr() {
		return remoteAttr;
	}

	public Collection<String> getRowKeys() {
		return rowKeys;
	}

	public String getSuperiorId() {
		return superiorId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getToken() {
		return token;
	}

	public int getUserType() {
		return userType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actorId == null) ? 0 : actorId.hashCode());
		return result;
	}

	public boolean isSystemAdministrator() {
        return "admin".equals(actorId);
    }

	public User jsonToObject(JSONObject json) {
		return UserJsonFactory.jsonToObject(json);
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public void setAdminFlag(String adminFlag) {
		this.adminFlag = adminFlag;
	}

	public void setComputerId(String computerId) {
		this.computerId = computerId;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setDomainIndex(int domainIndex) {
		this.domainIndex = domainIndex;
	}

	public void setDumpflag(int dumpflag) {
		this.dumpflag = dumpflag;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setIsBind(String isBind) {
		this.isBind = isBind;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public void setLocked(int locked) {
		this.locked = locked;
	}

	public void setLoginIP(String loginIP) {
		this.loginIP = loginIP;
	}

	public void setLoginRetry(int loginRetry) {
		this.loginRetry = loginRetry;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setMailPwd(String mailPwd) {
		this.mailPwd = mailPwd;
	}

	public void setMailUser(String mailUser) {
		this.mailUser = mailUser;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setPrincipalshipCode(String principalshipCode) {
		this.principalshipCode = principalshipCode;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setRemoteAttr(Integer remoteAttr) {
		this.remoteAttr = remoteAttr;
	}

	public void setRowKeys(Collection<String> rowKeys) {
		this.rowKeys = rowKeys;
	}

	public void setSuperiorId(String superiorId) {
		this.superiorId = superiorId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public JSONObject toJsonObject() {
		return UserJsonFactory.toJsonObject(this);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}