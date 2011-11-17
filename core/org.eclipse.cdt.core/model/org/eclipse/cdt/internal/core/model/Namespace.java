/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


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
	@Override
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

	/*
	 * @see org.eclipse.cdt.internal.core.model.CElement#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof INamespace && equals(this, (INamespace) other)) {
			return true;
		}
		return false;
	}
}
