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

	public VariableDeclaration(ICElement parent, String name, int type) {
		super(parent, name, type);
	}

	public String getTypeName() {
		return getVariableInfo().getTypeName();
	}

	public void setTypeName(String type) {
		getVariableInfo().setTypeString(type);
	}

	public boolean isConst() {
		return getVariableInfo().isConst();
	}

	public void setConst(boolean isConst) {
		getVariableInfo().setConst(isConst);
	}

	public boolean isVolatile() {
		return getVariableInfo().isVolatile();
	}

	public void setVolatile(boolean isVolatile) {
		getVariableInfo().setVolatile(isVolatile);
	}

	public boolean isStatic() {
		return getVariableInfo().isStatic();
	}

	public void setStatic(boolean isStatic) {
		getVariableInfo().setStatic(isStatic);
	}

	public VariableInfo getVariableInfo(){
		return (VariableInfo) getElementInfo();
	}
	
	protected CElementInfo createElementInfo () {
		return new VariableInfo(this);
	}
}
