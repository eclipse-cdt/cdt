/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class CharValue extends IntegralValue implements ICDICharValue {

	/**
	 * @param v
	 */
	public CharValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDICharValue#getValue()
	 */
	public char getValue() throws CDIException {
		// TODO Auto-generated method stub
		return 0;
	}

}
