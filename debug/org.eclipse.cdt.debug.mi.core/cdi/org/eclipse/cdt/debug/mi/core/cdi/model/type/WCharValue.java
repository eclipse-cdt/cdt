/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class WCharValue extends IntegralValue implements ICDIWCharValue {

	/**
	 * @param v
	 */
	public WCharValue(Variable v) {
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
