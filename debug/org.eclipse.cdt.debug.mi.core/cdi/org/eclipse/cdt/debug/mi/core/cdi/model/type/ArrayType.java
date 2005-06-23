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

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class ArrayType extends DerivedType implements ICDIArrayType {

	int dimension;

	/**
	 * @param typename
	 */
	public ArrayType(Target target, String typename,int dim) {
		super(target, typename);
		dimension = dim;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType#getDimension()
	 */
	public int getDimension() {
		if (derivedType == null) {
			getComponentType();
		}
		return dimension;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIArrayType#getComponentType()
	 */
	public ICDIType getComponentType() {
		if (derivedType == null) {
			String orig = getTypeName();
			String name = orig;
			int lbracket = orig.lastIndexOf('[');
			int rbracket = orig.lastIndexOf(']');
			if (lbracket != -1 && rbracket != -1 && (rbracket > lbracket)) {
				try {
					String dim = name.substring(lbracket + 1, rbracket).trim();
					dimension = Integer.parseInt(dim);
				} catch (NumberFormatException e) {
				}
				name = orig.substring(0, lbracket).trim();
			}
			setComponentType(name);
		}
		return derivedType;
	}

}
