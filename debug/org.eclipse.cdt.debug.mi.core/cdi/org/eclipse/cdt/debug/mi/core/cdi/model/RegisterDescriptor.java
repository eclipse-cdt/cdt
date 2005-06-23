/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;

/**
 */
public class RegisterDescriptor extends VariableDescriptor implements ICDIRegisterDescriptor {


	public RegisterDescriptor(Target target, Thread thread, StackFrame frame, String name, String fn, int pos, int depth) {
		super(target, thread, frame, name, fn, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.VariableDescriptor#getFullName()
	 */
	public String getFullName() {
		if (fFullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fFullName = "$" + n; //$NON-NLS-1$
			}
		}
		return fFullName;
	}
}
