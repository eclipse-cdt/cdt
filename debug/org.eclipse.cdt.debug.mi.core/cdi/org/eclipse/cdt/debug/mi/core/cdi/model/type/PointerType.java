/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class PointerType extends DerivedType implements ICDIPointerType {

	public PointerType(Target target, String typename) {
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
