/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
		String valueString = getValueString();
		if (isNaN(valueString))
			result = Double.NaN;
		else if (isNegativeInfinity(valueString))
			result = Double.NEGATIVE_INFINITY;
		else if (isPositiveInfinity(valueString))
			result = Double.POSITIVE_INFINITY;
		else {		
			try {
				result = Double.parseDouble(valueString);
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
		String valueString = getValueString();
		if (isNaN(valueString))
			result = Float.NaN;
		else if (isNegativeInfinity(valueString))
			result = Float.NEGATIVE_INFINITY;
		else if (isPositiveInfinity(valueString))
			result = Float.POSITIVE_INFINITY;
		else {		
			try {
				result = Float.parseFloat(valueString);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	private boolean isPositiveInfinity(String valueString) {
		return (valueString != null) ? valueString.indexOf("inf") != -1 : false; //$NON-NLS-1$
	}

	private boolean isNegativeInfinity(String valueString) {
		return (valueString != null) ? valueString.indexOf("-inf") != -1 : false; //$NON-NLS-1$
	}

	private boolean isNaN(String valueString) {
		return (valueString != null) ? valueString.indexOf("nan") != -1 : false; //$NON-NLS-1$
	}
}
