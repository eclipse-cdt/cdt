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
		try {
			result = Double.parseDouble( getValueString() );
		}
		catch (NumberFormatException e) {
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#floatValue()
	 */
	public float floatValue() throws CDIException {
		float result = 0;
		try {
			result = Float.parseFloat( getValueString() );
		}
		catch (NumberFormatException e) {
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#longValue()
	 */
	public long longValue() throws CDIException {
		Double dbl = new Double( doubleValue() );
		return dbl.longValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#isInfinite()
	 */
	public boolean isInfinite() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue#isNaN()
	 */
	public boolean isNaN() {
		String valueString = null;
		try {
			valueString = getValueString();
		}
		catch (CDIException e) {
		}
		return ( valueString != null ) ? valueString.indexOf( "nan" ) != -1 : false;
	}
}
