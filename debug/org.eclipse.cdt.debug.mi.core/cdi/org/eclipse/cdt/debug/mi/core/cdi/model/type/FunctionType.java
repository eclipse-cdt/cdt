/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class FunctionType extends DerivedType implements ICDIFunctionType {

	String params = ""; //$NON-NLS-1$

	public FunctionType(Target target, String typename) {
		super(target, typename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType#getComponentType()
	 */
	public ICDIType getComponentType() {
		if (derivedType != null) {
			String orig = getTypeName();
			String name = orig;
			int lparen = orig.lastIndexOf('(');
			int rparen = orig.lastIndexOf(')');
			if (lparen != -1 && rparen != -1 && (rparen > lparen)) {
				params = name.substring(lparen + 1, rparen).trim();
				name = orig.substring(0, lparen).trim();
			}
			setComponentType(name);
		}
		return derivedType; 
	}

}
