/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITypeDef;

public class TypeDef extends SourceManipulation implements ITypeDef {
	String typeName = ""; //$NON-NLS-1$

	public TypeDef(ICElement parent, String name) {
		super(parent, name, ICElement.C_TYPEDEF);
	}

	/**
	 * Returns the typeName.
	 * @return String
	 */
	@Override
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
