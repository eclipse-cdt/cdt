/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 */
public class ArrayType extends DerivedType implements ICDIArrayType {

	/**
	 * @param typename
	 */
	public ArrayType(String typename) {
		super(typename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIArrayType#getComponentType()
	 */
	public ICDIType getComponentType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType#getDimension()
	 */
	public int getDimension() {
		return 0;
	}

}
