/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public class EnumValue extends IntegralValue implements ICDIEnumValue {

	/**
	 * @param v
	 */
	public EnumValue(Variable v) {
		super(v);
	}

}
