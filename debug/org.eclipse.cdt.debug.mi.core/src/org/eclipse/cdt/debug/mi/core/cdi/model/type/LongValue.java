/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class LongValue extends IntegralValue implements ICDILongValue {

	/**
	 * @param v
	 */
	public LongValue(Variable v) {
		super(v);
	}

}
