package com.robotium.solo;

import java.util.ArrayList;

public class Params {
	
	public boolean iteration;

	public boolean isWeb() {
		return web;
	}

	public void setWeb(boolean web) {
		this.web = web;
	}

	public boolean web;

	public boolean isIteration() {
		return iteration;
	}

	public void setIteration(boolean iteration) {
		this.iteration = iteration;
	}

	public ArrayList<Param> getParams() {
		return params;
	}

	public void setParams(ArrayList<Param> params) {
		this.params = params;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String name;
	public ArrayList<Param> params;

	@Override
	public boolean equals(Object o) {
		if (o instanceof Params) {
			Params param = (Params) o;
			return getName().equals(param.getName());
		}
		return super.equals(o);
	}

}
