package com.robotium.solo;

import java.util.ArrayList;

public class ParamsEntity {

	private boolean iteration;

	private boolean web;

	private String name;
	private ArrayList<ParamEntity> params;

	public boolean isWeb() {
		return web;
	}

	public void setWeb(boolean web) {
		this.web = web;
	}

	public boolean isIteration() {
		return iteration;
	}

	public void setIteration(boolean iteration) {
		this.iteration = iteration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ParamEntity> getParams() {
		return params;
	}

	public void setParams(ArrayList<ParamEntity> params) {
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
