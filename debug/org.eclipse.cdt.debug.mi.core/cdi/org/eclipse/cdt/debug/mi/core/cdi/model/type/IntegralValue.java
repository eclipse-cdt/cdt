/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Value;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public abstract class IntegralValue extends Value implements ICDIIntegralValue {

	/**
	 * @param v
	 */
	public IntegralValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#longValue()
	 */
	public long longValue() throws CDIException {
		long value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = Long.decode(valueString).longValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#longValue()
	 */
	public int intValue() throws CDIException {
		int value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = Integer.decode(valueString).intValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#shortValue()
	 */
	public short shortValue() throws CDIException {
		short value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = Short.decode(valueString).shortValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#byteValue()
	 */
	public int byteValue() throws CDIException {
		byte value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = Byte.decode(valueString).byteValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

}
