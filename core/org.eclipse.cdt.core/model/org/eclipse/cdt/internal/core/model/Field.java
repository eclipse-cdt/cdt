package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class Field extends VariableDeclaration implements IField {
	
	public Field(ICElement parent, String name) {
		super(parent, name, CElement.C_FIELD);
	}

	public boolean isMutable() throws CModelException{
		return getFieldInfo().isMutable();
	}

	public void setMutable(boolean mutable) throws CModelException{
		getFieldInfo().setMutable(mutable);
	}

	public String getTypeName() throws CModelException {
		return getFieldInfo().getTypeName();
	}

	public void setTypeName(String type) throws CModelException {
		getFieldInfo().setTypeName(type);
	}

	public boolean isConst() throws CModelException {
		return getFieldInfo().isConst();
	}

	public void setConst(boolean isConst) throws CModelException {
		getFieldInfo().setConst(isConst);
	}

	public boolean isVolatile() throws CModelException {
		return getFieldInfo().isVolatile();
	}

	public void setVolatile(boolean isVolatile) throws CModelException {
		getFieldInfo().setVolatile(isVolatile);
	}

	public boolean isStatic() throws CModelException {
		return getFieldInfo().isStatic();
	}

	public void setStatic(boolean isStatic) throws CModelException {
		getFieldInfo().setStatic(isStatic);
	}

	public ASTAccessVisibility getVisibility() throws CModelException {
		return getFieldInfo().getVisibility();
	}

	public void setVisibility(ASTAccessVisibility visibility) throws CModelException {
		getFieldInfo().setVisibility(visibility);
	}

	public FieldInfo getFieldInfo() throws CModelException{
		return (FieldInfo) getElementInfo();
	}

	protected CElementInfo createElementInfo () {
		return new FieldInfo(this);
	}
}
