/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class IntValue extends IntegralValue implements ICDIIntValue {

	/**
	 * @param v
	 */
	public IntValue(Variable v) {
		super(v);
	}

}
