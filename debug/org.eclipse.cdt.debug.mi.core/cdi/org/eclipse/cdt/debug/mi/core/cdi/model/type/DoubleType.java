/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;

/**
 */
public class DoubleType extends FloatingPointType implements ICDIDoubleType {

	/**
	 * @param typename
	 */
	public DoubleType(ICDITarget target, String typename) {
		this(target, typename, false, false, false);
	}

	public DoubleType(ICDITarget target, String typename, boolean isComplex, boolean isImg, boolean isLong) {
		super(target, typename, isComplex, isImg, isLong);
	}
}
