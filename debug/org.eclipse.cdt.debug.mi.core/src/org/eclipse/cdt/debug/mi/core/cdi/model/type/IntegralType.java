/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralType;

/**
 */
public abstract class IntegralType extends Type implements ICDIIntegralType {

	boolean unSigned;

	public IntegralType(String typename, boolean usigned) {
		super(typename);
		unSigned = usigned;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralType#isUnsigned()
	 */
	public boolean isUnsigned() {
		return unSigned;
	}

}
