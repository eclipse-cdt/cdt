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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;

public class Enumeration extends SourceManipulation implements IEnumeration{

	public Enumeration(ICElement parent, String name) {
		super(parent, name, CElement.C_ENUMERATION);		
	}

	protected CElementInfo createElementInfo () {
		return new EnumerationInfo(this);
	}

	private EnumerationInfo getEnumerationInfo(){
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
	public String getTypeName() {
		return getEnumerationInfo().getTypeName();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IVariableDeclaration#setTypeName(java.lang.String)
	 */
	public void setTypeName(String type) {
		getEnumerationInfo().setTypeName(type);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#getAccessControl()
	 */
	public int getAccessControl() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst() {
		return getEnumerationInfo().isConst();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isStatic()
	 */
	public boolean isStatic() {
		return getEnumerationInfo().isStatic();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isVolatile()
	 */
	public boolean isVolatile() {
		return getEnumerationInfo().isVolatile();
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		getEnumerationInfo().setConst(isConst);
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		getEnumerationInfo().setStatic( isStatic);
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		getEnumerationInfo().setVolatile(isVolatile);
	}	

}
