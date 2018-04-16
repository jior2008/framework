package com.glaf.core.base;

public class JsonResult {

	protected String status = null;

	protected Object result = null;

	public JsonResult() {

	}

	public Object getResult() {
		return result;
	}

	public String getStatus() {
		return status;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public JsonResult status(String status) {
		this.status = status;
		return this;
	}

}
