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

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class ReferenceType extends DerivedType implements ICDIReferenceType {

	/**
	 * @param name
	 */
	public ReferenceType(Target target, String name) {
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
