/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class LongLongValue extends IntegralValue implements ICDILongLongValue {

	/**
	 * @param v
	 */
	public LongLongValue(Variable v) {
		super(v);
	}

}
