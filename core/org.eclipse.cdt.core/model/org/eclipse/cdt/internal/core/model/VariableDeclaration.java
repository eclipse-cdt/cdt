package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariableDeclaration;

public class VariableDeclaration extends SourceManipulation implements IVariableDeclaration {
	
	public VariableDeclaration(ICElement parent, String name) {
		super(parent, name, ICElement.C_VARIABLE_DECLARATION);
	}

	public VariableDeclaration(ICElement parent, String name, int type) {
		super(parent, name, type);
	}

	public String getTypeName() throws CModelException {
		return getVariableInfo().getTypeName();
	}

	public void setTypeName(String type) throws CModelException {
		getVariableInfo().setTypeString(type);
	}

	public boolean isConst() throws CModelException {
		return getVariableInfo().isConst();
	}

	public void setConst(boolean isConst) throws CModelException {
		getVariableInfo().setConst(isConst);
	}

	public boolean isVolatile() throws CModelException {
		return getVariableInfo().isVolatile();
	}

	public void setVolatile(boolean isVolatile) throws CModelException {
		getVariableInfo().setVolatile(isVolatile);
	}

	public boolean isStatic() throws CModelException {
		return getVariableInfo().isStatic();
	}

	public void setStatic(boolean isStatic) throws CModelException {
		getVariableInfo().setStatic(isStatic);
	}

	public VariableInfo getVariableInfo() throws CModelException{
		return (VariableInfo) getElementInfo();
	}
	
	protected CElementInfo createElementInfo () {
		return new VariableInfo(this);
	}
}
