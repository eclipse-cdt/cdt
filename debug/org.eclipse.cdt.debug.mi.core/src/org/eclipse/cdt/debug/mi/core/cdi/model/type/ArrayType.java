/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 */
public class ArrayType extends DerivedType implements ICDIArrayType {

	int dimension;

	/**
	 * @param typename
	 */
	public ArrayType(ICDITarget target, String typename,int dim) {
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
