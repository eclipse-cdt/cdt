/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortType;

/**
 */
public class ShortType extends IntegralType implements ICDIShortType {

	/**
	 * @param typename
	 */
	public ShortType(String typename) {
		this(typename, false);
	}

	public ShortType(String typename, boolean usigned) {
		super(typename, usigned);
	}
}
