/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class FloatValue extends FloatingPointValue implements ICDIFloatValue {

	/**
	 * @param Variable
	 */
	public FloatValue(Variable v) {
		super(v);
	}

}
