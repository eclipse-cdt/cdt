/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class Field extends VariableDeclaration implements IField {
	
	public Field(ICElement parent, String name) {
		super(parent, name, ICElement.C_FIELD);
	}

	public boolean isMutable() throws CModelException{
		return getFieldInfo().isMutable();
	}

	public void setMutable(boolean mutable) throws CModelException{
		getFieldInfo().setMutable(mutable);
	}

	@Override
	public String getTypeName() throws CModelException {
		return getFieldInfo().getTypeName();
	}

	@Override
	public void setTypeName(String type) throws CModelException {
		getFieldInfo().setTypeName(type);
	}

	@Override
	public boolean isConst() throws CModelException {
		return getFieldInfo().isConst();
	}

	@Override
	public void setConst(boolean isConst) throws CModelException {
		getFieldInfo().setConst(isConst);
	}

	@Override
	public boolean isVolatile() throws CModelException {
		return getFieldInfo().isVolatile();
	}

	@Override
	public void setVolatile(boolean isVolatile) throws CModelException {
		getFieldInfo().setVolatile(isVolatile);
	}

	@Override
	public boolean isStatic() throws CModelException {
		return getFieldInfo().isStatic();
	}

	@Override
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

	@Override
	protected CElementInfo createElementInfo () {
		return new FieldInfo(this);
	}
}
