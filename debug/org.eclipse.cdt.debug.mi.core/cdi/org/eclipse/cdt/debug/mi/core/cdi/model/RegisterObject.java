/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;

/**
 */
public class RegisterObject extends VariableObject implements ICDIRegisterObject {

	public RegisterObject(Target target, String name, int i) {
		super(target, name, null, i, 0);
	}

	public RegisterObject(Target target, String name, String fn, int i) {
		super(target, name, fn, null, i, 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject#getFullName()
	 */
	public String getFullName() {
		if (fullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fullName = "$" + n; //$NON-NLS-1$
			}
		}
		return fullName;
	}
}
