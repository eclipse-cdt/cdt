/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.Value;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 */
public abstract class DerivedValue extends Value implements ICDIDerivedValue {

	public DerivedValue(Variable v) {
		super(v);
	}

}
