/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 */
public class FunctionType extends DerivedType implements ICDIFunctionType {

	String params = ""; //$NON-NLS-1$

	public FunctionType(ICDITarget target, String typename) {
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
