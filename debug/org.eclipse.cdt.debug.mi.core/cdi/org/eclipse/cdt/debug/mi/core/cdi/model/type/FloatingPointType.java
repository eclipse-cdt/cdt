/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public abstract class FloatingPointType extends Type implements ICDIFloatingPointType {

	boolean complex;
	boolean imaginary;
	boolean islong;

	public FloatingPointType(Target target, String typename, boolean comp, boolean img, boolean l) {
		super(target, typename);
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
