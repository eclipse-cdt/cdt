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

public class StructureInfo extends VariableInfo {
	
	protected StructureInfo (CElement element) {
		super(element);
		
		if (element.getElementType() == ICElement.C_CLASS)
			this.setTypeName("class");
		else if (element.getElementType() == ICElement.C_UNION)
			this.setTypeName("union");
		else
			this.setTypeName("struct");
	}

	public boolean isUnion() {
		return element.getElementType() == ICElement.C_UNION;
	}

	public boolean isClass() {
		return element.getElementType() == ICElement.C_CLASS;
	}

	public boolean isStruct() {
		return element.getElementType() == ICElement.C_STRUCT;
	}

}

