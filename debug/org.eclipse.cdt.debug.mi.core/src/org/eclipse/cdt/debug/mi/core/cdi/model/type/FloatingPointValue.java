/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#doubleValue()
	 */
	public double doubleValue() throws CDIException {
		double result = 0;
		if (isNaN())
			result = Double.NaN;
		else if (isNegativeInfinity())
			result = Double.NEGATIVE_INFINITY;
		else if (isPositiveInfinity())
			result = Double.POSITIVE_INFINITY;
		else {		
			try {
				result = Double.parseDouble(getValueString());
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#floatValue()
	 */
	public float floatValue() throws CDIException {
		float result = 0;
		if (isNaN())
			result = Float.NaN;
		else if (isNegativeInfinity())
			result = Float.NEGATIVE_INFINITY;
		else if (isPositiveInfinity())
			result = Float.POSITIVE_INFINITY;
		else {		
			try {
				result = Float.parseFloat(getValueString());
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	private boolean isPositiveInfinity() throws CDIException {
		String valueString = getValueString();
		return (valueString != null) ? valueString.indexOf("inf") != -1 : false;
	}

	private boolean isNegativeInfinity() throws CDIException {
		String valueString = getValueString();
		return (valueString != null) ? valueString.indexOf("-inf") != -1 : false;
	}

	private boolean isNaN() throws CDIException {
		String valueString = getValueString();
		return (valueString != null) ? valueString.indexOf("nan") != -1 : false;
	}
}
