package org.eclipse.cdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;

public class Enumeration extends SourceManipulation implements IEnumeration{

	public Enumeration(ICElement parent, String name) {
		super(parent, name, ICElement.C_ENUMERATION);		
	}

	protected CElementInfo createElementInfo () {
		return new EnumerationInfo(this);
	}

	private EnumerationInfo getEnumerationInfo() throws CModelException{
		return (EnumerationInfo) getElementInfo();
	}
	/**
	 * @see org.eclipse.cdt.core.model.IVariable#getInitializer()
	 */
	public String getInitializer() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IVariableDeclaration#getTypeName()
	 */
	public String getTypeName() throws CModelException {
		return getEnumerationInfo().getTypeName();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IVariableDeclaration#setTypeName(java.lang.String)
	 */
	public void setTypeName(String type) throws CModelException {
		getEnumerationInfo().setTypeName(type);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst() throws CModelException {
		return getEnumerationInfo().isConst();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isStatic()
	 */
	public boolean isStatic() throws CModelException {
		return getEnumerationInfo().isStatic();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isVolatile()
	 */
	public boolean isVolatile() throws CModelException {
		return getEnumerationInfo().isVolatile();
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) throws CModelException {
		getEnumerationInfo().setConst(isConst);
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) throws CModelException {
		getEnumerationInfo().setStatic( isStatic);
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) throws CModelException {
		getEnumerationInfo().setVolatile(isVolatile);
	}	

}
