/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;

/**
 */
public class BoolType extends IntegralType implements ICDIBoolType {

	/**
	 * @param typename
	 */
	public BoolType(String typename) {
		this(typename, false);
	}

	public BoolType(String typename, boolean usigned) {
		super(typename, usigned);
	}

}
