/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

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

	@Override
	public String getTypeName() throws CModelException {
		return getVariableInfo().getTypeName();
	}

	@Override
	public void setTypeName(String type) throws CModelException {
		getVariableInfo().setTypeString(type);
	}

	@Override
	public boolean isConst() throws CModelException {
		return getVariableInfo().isConst();
	}

	public void setConst(boolean isConst) throws CModelException {
		getVariableInfo().setConst(isConst);
	}

	@Override
	public boolean isVolatile() throws CModelException {
		return getVariableInfo().isVolatile();
	}

	public void setVolatile(boolean isVolatile) throws CModelException {
		getVariableInfo().setVolatile(isVolatile);
	}

	@Override
	public boolean isStatic() throws CModelException {
		return getVariableInfo().isStatic();
	}

	public void setStatic(boolean isStatic) throws CModelException {
		getVariableInfo().setStatic(isStatic);
	}

	public VariableInfo getVariableInfo() throws CModelException{
		return (VariableInfo) getElementInfo();
	}

	@Override
	protected CElementInfo createElementInfo () {
		return new VariableInfo(this);
	}
}
