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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Argument extends Variable implements ICDIArgument {

	public Argument(Target target, Thread thread, StackFrame frame,
			String n, String q, int pos, int depth, MIVar v) {
		super(target, thread, frame, n, q, pos, depth, v);
	}

	public Argument(ArgumentDescriptor obj, MIVarCreate var) {
		super(obj, var);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.Variable#createVariable(org.eclipse.cdt.debug.mi.core.cdi.model.Target, org.eclipse.cdt.debug.core.cdi.model.ICDIThread, org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame, java.lang.String, java.lang.String, int, int, org.eclipse.cdt.debug.mi.core.output.MIVar)
	 */
	protected Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, MIVar miVar) {
		return new Argument(target, thread, frame, name, fullName, pos, depth, miVar);
	}

}
