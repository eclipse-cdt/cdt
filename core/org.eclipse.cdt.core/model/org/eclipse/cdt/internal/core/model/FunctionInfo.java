package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class FunctionInfo extends SourceManipulationInfo {

	protected int flags;
	protected String returnType = "";
	protected int numOfParams;
	protected boolean isStatic;
	protected boolean isVolatile;
	

	protected FunctionInfo (CElement element) {
		super(element);
		flags = 0;
	}

	protected int getAccessControl() {
		return flags;
	}

	protected void setAccessControl(int flags) {
		this.flags = flags;
	}

	protected String getReturnType(){
		return returnType;
	}
	
	protected void setReturnType(String type){
		returnType = type;
	}	
	/**
	 * Returns the isStatic.
	 * @return boolean
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Returns the isVolatile.
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

}
