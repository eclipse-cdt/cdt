package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class VariableInfo extends SourceManipulationInfo {

	String typeStr = ""; //$NON-NLS-1$
	boolean isConst = false;
	boolean isVolatile = false;
	boolean isStatic = false;
	
	protected VariableInfo (CElement element) {
		super(element);
	}

	protected String getTypeName(){
		return typeStr;
	}
	
	protected void setTypeName(String type){
		typeStr = type;
	}
	
	protected void setTypeString(String type){
		typeStr = type;
	}
	protected boolean isConst(){
		return isConst;
	}

	protected void setConst(boolean isConst){
		this.isConst = isConst;
	}

	protected boolean isVolatile(){
		return isVolatile;
	}

	protected void setVolatile(boolean isVolatile){
		this.isVolatile = isVolatile;
	}

	protected boolean isStatic() {
		return isStatic;
	}

	protected void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.SourceManipulationInfo#hasSameContentsAs(org.eclipse.cdt.internal.core.model.SourceManipulationInfo)
	 */
	public boolean hasSameContentsAs(SourceManipulationInfo otherInfo) {
		return 
		(	 super.hasSameContentsAs(otherInfo) 
		&& ( typeStr.equals(((VariableInfo)otherInfo).getTypeName()) )
		&& ( isConst() == ((VariableInfo)otherInfo).isConst() ) 
		&& (isVolatile() == ((VariableInfo)otherInfo).isVolatile() ) 
		&& (isStatic() == ((VariableInfo)otherInfo).isStatic() )
		);
	}

}
