/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;

/**
 */
public class EnumType extends IntegralType implements ICDIEnumType {

	/**
	 * @param typename
	 */
	public EnumType(String typename) {
		this(typename, false);
	}

	public EnumType(String typename, boolean usigned) {
		super(typename, usigned);
	}
}
