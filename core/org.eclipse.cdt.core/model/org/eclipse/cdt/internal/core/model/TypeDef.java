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
import org.eclipse.cdt.core.model.ITypeDef;

public class TypeDef extends SourceManipulation implements ITypeDef{
	
	String typeName= ""; //$NON-NLS-1$
	public TypeDef(ICElement parent, String name) {
		super(parent, name, ICElement.C_TYPEDEF);
	}
	/**
	 * Returns the typeName.
	 * @return String
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Sets the typeName.
	 * @param typeName The typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

}
