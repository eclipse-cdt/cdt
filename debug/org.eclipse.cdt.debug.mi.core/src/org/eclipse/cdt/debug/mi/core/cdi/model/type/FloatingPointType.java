/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType;

/**
 */
public abstract class FloatingPointType extends Type implements ICDIFloatingPointType {

	boolean complex;
	boolean imaginary;
	boolean islong;

	public FloatingPointType(String typename, boolean comp, boolean img, boolean l) {
		super(typename);
		complex = comp;
		imaginary = img;
		islong = l;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType#isComplex()
	 */
	public boolean isComplex() {
		return complex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType#isImaginary()
	 */
	public boolean isImaginary() {
		return imaginary;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType#isLong()
	 */
	public boolean isLong() {
		return islong;
	}

}
