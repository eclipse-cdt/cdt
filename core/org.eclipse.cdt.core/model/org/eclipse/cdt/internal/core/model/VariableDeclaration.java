package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariableDeclaration;

public class VariableDeclaration extends SourceManipulation implements IVariableDeclaration {
	
	public VariableDeclaration(ICElement parent, String name) {
		super(parent, name, CElement.C_VARIABLE_DECLARATION);
	}

	public int getAccessControl() {
		return getVariableInfo().getAccessControl();
	}

	public String getTypeName() {
		return getVariableInfo().getTypeName();
	}

	public void setTypeName(String type) {
		getVariableInfo().setTypeString(type);
	}

	public VariableInfo getVariableInfo(){
		return (VariableInfo) getElementInfo();
	}
	
	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
