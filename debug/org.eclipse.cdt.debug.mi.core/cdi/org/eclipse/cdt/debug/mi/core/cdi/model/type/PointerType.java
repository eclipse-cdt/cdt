/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 */
public class PointerType extends DerivedType implements ICDIPointerType {

	public PointerType(ICDITarget target, String typename) {
		super(target, typename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType#getComponentType()
	 */
	public ICDIType getComponentType() {
		if (derivedType == null) {
			String orig = getTypeName();
			String name = orig;
			int star = orig.lastIndexOf('*');
			// remove last '*'
			if (star != -1) { 
				name = orig.substring(0, star).trim();
			}
			setComponentType(name);
		}
		return derivedType;
	}

}
