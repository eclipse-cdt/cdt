/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;

/**
 */
public class IntType extends IntegralType implements ICDIIntType {

	/**
	 * @param typename
	 */
	public IntType(String typename) {
		this(typename, false);
	}

	public IntType(String typename, boolean usigned) {
		super(typename, usigned);
	}

}
