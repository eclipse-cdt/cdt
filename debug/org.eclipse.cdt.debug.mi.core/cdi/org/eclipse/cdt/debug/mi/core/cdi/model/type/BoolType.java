/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;

/**
 */
public class BoolType extends IntegralType implements ICDIBoolType {

	/**
	 * @param typename
	 */
	public BoolType(ICDITarget target, String typename) {
		this(target, typename, false);
	}

	public BoolType(ICDITarget target, String typename, boolean usigned) {
		super(target, typename, usigned);
	}

}
