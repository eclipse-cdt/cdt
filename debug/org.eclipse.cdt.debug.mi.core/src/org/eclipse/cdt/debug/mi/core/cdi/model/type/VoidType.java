/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIVoidType;

/**
 */
public class VoidType extends Type implements ICDIVoidType {

	public VoidType(String typename) {
		super(typename);
	}
}
