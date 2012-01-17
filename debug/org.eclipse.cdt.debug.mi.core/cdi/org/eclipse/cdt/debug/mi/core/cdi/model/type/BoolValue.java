/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Warren Paul (Nokia) - 150860
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
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

	@Override
	public BigInteger bigIntegerValue() throws CDIException {
		String valueString = getValueString();
		if (valueString.equalsIgnoreCase("false"))//$NON-NLS-1$
			return BigInteger.ZERO;
		else
		if (valueString.equalsIgnoreCase("true"))//$NON-NLS-1$
			return BigInteger.ONE;
		
		return super.bigIntegerValue();
	}

}
