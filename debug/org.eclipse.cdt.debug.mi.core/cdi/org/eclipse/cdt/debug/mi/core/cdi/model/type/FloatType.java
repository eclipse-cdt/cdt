/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;

/**
 */
public class FloatType extends FloatingPointType implements ICDIFloatType {

	/**
	 * @param typename
	 */
	public FloatType(ICDITarget target, String typename) {
		this(target, typename, false, false);
	}

	public FloatType(ICDITarget target, String typename, boolean isComplex, boolean isImg) {
		super(target, typename, isComplex, isImg, false);
	}
}
