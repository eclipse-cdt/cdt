/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class DoubleValue extends FloatingPointValue implements ICDIDoubleValue {

	/**
	 * @param Variable
	 */
	public DoubleValue(Variable v) {
		super(v);
	}
}
