package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class VariableInfo extends SourceManipulationInfo {

	protected int flags;
	String typeStr = "";
	
	protected VariableInfo (CElement element) {
		super(element);
		flags = 0;
	}

	protected int getAccessControl() {
		return flags;
	}

	protected String getTypeName(){
		return typeStr;
	}
	
	protected void setTypeName(String type){
		typeStr = type;
	}

	protected boolean hasSameContentsAs( VariableInfo otherInfo){
		if(typeStr.equals(otherInfo.getTypeName())) 
			return true;
		else
			return false;
	}
	
	protected void setAccessControl(int flags) {
		this.flags = flags;
	}
	
	protected void setTypeString(String type){
		typeStr = type;
	}
}
