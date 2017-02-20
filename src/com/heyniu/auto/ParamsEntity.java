package com.heyniu.auto;

import java.util.ArrayList;

class ParamsEntity {

	private boolean iteration;

	private boolean web;

	private String name;
	private ArrayList<ParamEntity> params;

	boolean isWeb() {
		return web;
	}

	void setWeb(boolean web) {
		this.web = web;
	}

	boolean isIteration() {
		return iteration;
	}

	void setIteration(boolean iteration) {
		this.iteration = iteration;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	ArrayList<ParamEntity> getParams() {
		return params;
	}

	void setParams(ArrayList<ParamEntity> params) {
		this.params = params;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ParamsEntity) {
			ParamsEntity param = (ParamsEntity) o;
			return getName().equals(param.getName());
		}
		return super.equals(o);
	}

}
