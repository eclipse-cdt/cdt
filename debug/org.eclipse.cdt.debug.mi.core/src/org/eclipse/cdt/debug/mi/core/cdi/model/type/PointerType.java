/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;

/**
 */
public class PointerType extends DerivedType implements ICDIPointerType {

	public PointerType(String typename) {
		super(typename);
	}
}
