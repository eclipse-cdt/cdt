/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 */
public class ReferenceType extends DerivedType implements ICDIReferenceType {

	/**
	 * @param name
	 */
	public ReferenceType(ICDITarget target, String name) {
		super(target, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType#getComponentType()
	 */
	public ICDIType getComponentType() {
		if (derivedType == null) {
			String orig = getTypeName();
			String name = orig;
			int amp = orig.lastIndexOf('&');
			// remove last '&'
			if (amp != -1) { 
				name = orig.substring(0, amp).trim();
			}
			setComponentType(name);
		}
		return derivedType;
	}

}
