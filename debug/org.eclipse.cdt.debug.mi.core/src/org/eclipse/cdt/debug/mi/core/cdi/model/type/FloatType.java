/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;

/**
 */
public class FloatType extends FloatingPointType implements ICDIFloatType {

	/**
	 * @param typename
	 */
	public FloatType(String typename) {
		this(typename, false, false);
	}

	public FloatType(String typename, boolean isComplex, boolean isImg) {
		super(typename, isComplex, isImg, false);
	}
}
