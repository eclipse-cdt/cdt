/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */


package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;

/**
 */
public class WCharType extends IntegralType implements ICDIWCharType {

	/**
	 * @param typename
	 */
	public WCharType(ICDITarget target, String typename) {
		this(target, typename, false);
	}

	public WCharType(ICDITarget target, String typename, boolean usigned) {
		super(target, typename, usigned);
	}
}
