/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;

/**
 */
public class LongType extends IntegralType implements ICDILongType {

	/**
	 * @param typename
	 */
	public LongType(String typename) {
		this(typename, false);
	}

	public LongType(String typename, boolean usigned) {
		super(typename, usigned);
	}
}
