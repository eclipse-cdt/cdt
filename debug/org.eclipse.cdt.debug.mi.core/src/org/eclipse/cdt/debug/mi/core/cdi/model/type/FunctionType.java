/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;

/**
 */
public class FunctionType extends DerivedType implements ICDIFunctionType {

	public FunctionType(String typename) {
		super(typename);
	}
}
