/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 * Enter type comment.
 * 
 * @since Jun 3, 2003
 */
public class ReferenceValue extends DerivedValue implements ICDIReferenceValue {

	/**
	 * @param v
	 */
	public ReferenceValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue#referenceValue()
	 */
	public long referenceValue() throws CDIException {
		long value = 0;
		String valueString = getValueString().trim();
		if ( valueString.startsWith("@") )
			valueString = valueString.substring( 1 );
		int space = valueString.indexOf(":");
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = Long.decode(valueString).longValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}
}
