/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;

/**
 */
public class CharType extends IntegralType implements ICDICharType {

	/**
	 * @param typename
	 */
	public CharType(ICDITarget target, String typename) {
		this(target, typename, false);
	}

	public CharType(ICDITarget target, String typename, boolean usigned) {
		super(target, typename, usigned);
	}
}
