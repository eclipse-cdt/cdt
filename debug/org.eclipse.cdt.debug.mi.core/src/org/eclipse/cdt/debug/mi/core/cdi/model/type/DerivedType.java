/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 */
public abstract class DerivedType extends Type implements ICDIDerivedType {

	public DerivedType(String typename) {
		super(typename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType#getComponentType()
	 */
	public ICDIType getComponentType() {
		return null;
	}

}
