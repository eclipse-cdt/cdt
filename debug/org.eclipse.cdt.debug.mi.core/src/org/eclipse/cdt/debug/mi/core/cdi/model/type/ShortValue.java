/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class ShortValue extends IntegralValue implements ICDIShortValue {

	/**
	 * @param v
	 */
	public ShortValue(Variable v) {
		super(v);
	}

}
