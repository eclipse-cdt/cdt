/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class BoolValue extends IntegralValue implements ICDIBoolValue {

	/**
	 * @param v
	 */
	public BoolValue(Variable v) {
		super(v);
	}

}
