/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIAggregateType;

/**
 */
public abstract class AggregateType extends Type implements ICDIAggregateType {

	public AggregateType(String typename) {
		super(typename);
	}
}
