/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */


package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;

/**
 */
public class WCharType extends IntegralType implements ICDIWCharType {

	/**
	 * @param typename
	 */
	public WCharType(String typename) {
		this(typename, false);
	}

	public WCharType(String typename, boolean usigned) {
		super(typename, usigned);
	}
}
