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
import org.eclipse.cdt.core.model.INamespace;

public class Namespace extends SourceManipulation implements INamespace{

	String typeName = ""; //$NON-NLS-1$
	public Namespace(ICElement parent, String name) {
		super(parent, name, ICElement.C_NAMESPACE);
	}

	/*
	 * Returns the typeName.
	 * @return String
	 */
	public String getTypeName() {
		return typeName;
	}

	/*
	 * Sets the typeName.
	 * @param typeName The typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	public boolean equals(Object other) {
//		return (super.equals(other) 
//				&& (this.getStartPos() == ((Namespace)other).getStartPos())
//				&& (this.getLength() == ((Namespace)other).getLength())				
//		);
//	}

}
