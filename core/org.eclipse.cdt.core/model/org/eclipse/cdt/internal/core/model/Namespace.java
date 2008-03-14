/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.INamespace;

public class Namespace extends SourceManipulation implements INamespace{

	String typeName = ""; //$NON-NLS-1$
	int fIndex;
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

	/**
	 * Set the index of this namespace, in case the same namespace is referenced
	 * multiple times.
	 * 
	 * @param index
	 */
	public void setIndex(int index) {
		fIndex= index;
	}

	/*
	 * @see org.eclipse.cdt.internal.core.model.CElement#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof INamespace && equals(this, (INamespace) other)) {
			if (other instanceof Namespace) {
				return fIndex == ((Namespace)other).fIndex;
			}
			return true;
		}
		return false;
	}

}
