/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;

/**
 */
public class ReferenceType extends DerivedType implements ICDIReferenceType {

	/**
	 * @param name
	 */
	public ReferenceType(String name) {
		super(name);
	}

}
