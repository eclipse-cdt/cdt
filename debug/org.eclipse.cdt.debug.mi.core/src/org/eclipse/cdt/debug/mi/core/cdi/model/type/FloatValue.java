/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#isNaN()
	 */
	public boolean isNaN() {
		// Identify this value as Not-a-Number if parsing fails.
		try {
			Float.parseFloat( getValueString() );
		}
		catch (NumberFormatException e) {
			return true;
		}
		catch (CDIException e) {
			return true;
		}
		return false;
	}
}
