package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariable;

public class Variable extends SourceManipulation implements IVariable {
	
	public Variable(ICElement parent, String name) {
		super(parent, name, CElement.C_VARIABLE);
	}

	public String getTypeName() {
		return getVariableInfo().getTypeName();
	}

	public void setTypeName(String type){
		getVariableInfo().setTypeName(type);
	}
	public String getInitializer() {
		return "";
	}

	public int getAccessControl() {
		return getVariableInfo().getAccessControl();
	}

	protected VariableInfo getVariableInfo() {
		return (VariableInfo)getElementInfo();
	}

	protected CElementInfo createElementInfo () {
		return new VariableInfo(this);
	}
}
