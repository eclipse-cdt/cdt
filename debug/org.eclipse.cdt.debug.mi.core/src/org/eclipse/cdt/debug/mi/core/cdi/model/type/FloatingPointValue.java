/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Value;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public abstract class FloatingPointValue extends Value implements ICDIFloatingPointValue {

	/**
	 * @param v
	 */
	public FloatingPointValue(Variable v) {
		super(v);
	}

}
