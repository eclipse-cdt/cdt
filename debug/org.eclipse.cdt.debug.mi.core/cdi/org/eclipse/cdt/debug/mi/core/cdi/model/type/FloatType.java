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

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject;

/**
 */
public class FloatType extends FloatingPointType implements ICDIFloatType {

	/**
	 * @param typename
	 */
	public FloatType(VariableObject vo, String typename) {
		this(vo, typename, false, false);
	}

	public FloatType(VariableObject vo, String typename, boolean isComplex, boolean isImg) {
		super(vo, typename, isComplex, isImg, false);
	}
}
