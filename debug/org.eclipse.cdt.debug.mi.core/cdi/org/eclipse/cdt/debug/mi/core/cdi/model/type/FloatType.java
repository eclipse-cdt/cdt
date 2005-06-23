/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class FloatType extends FloatingPointType implements ICDIFloatType {

	/**
	 * @param typename
	 */
	public FloatType(Target target, String typename) {
		this(target, typename, false, false);
	}

	public FloatType(Target target, String typename, boolean isComplex, boolean isImg) {
		super(target, typename, isComplex, isImg, false);
	}
}
