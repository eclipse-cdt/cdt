package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class FunctionInfo extends SourceManipulationInfo {

	protected boolean isStatic;
	protected boolean isVolatile;
	protected boolean isConst;
	

	protected FunctionInfo (CElement element) {
		super(element);
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

	/**
	 * @see org.eclipse.cdt.internal.core.model.SourceManipulationInfo#hasSameContentsAs(org.eclipse.cdt.internal.core.model.SourceManipulationInfo)
	 */
	public boolean hasSameContentsAs(SourceManipulationInfo otherInfo) {
		return (super.hasSameContentsAs(otherInfo)
		&& (this.isStatic() == ((FunctionInfo)otherInfo).isStatic())
		&& (this.isVolatile() == ((FunctionInfo)otherInfo).isVolatile())
		&& (this.isConst() == ((FunctionInfo)otherInfo).isConst())
		);
	}

	/**
	 * Returns the isConst.
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

}
