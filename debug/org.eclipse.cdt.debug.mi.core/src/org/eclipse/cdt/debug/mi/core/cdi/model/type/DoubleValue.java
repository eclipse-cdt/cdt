/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#isNaN()
	 */
	public boolean isNaN() {
		// Identify this value as Not-a-Number if parsing fails.
		try {
			Double.parseDouble( getValueString() );
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
