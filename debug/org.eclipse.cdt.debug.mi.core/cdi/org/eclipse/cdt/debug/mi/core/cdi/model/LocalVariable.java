/*******************************************************************************
 * Copyright (c) 2002, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 * LocalVariable
 */
public class LocalVariable extends Variable implements ICDILocalVariable {

	/**
	 * @param obj
	 * @param v
	 */
	public LocalVariable(LocalVariableDescriptor obj, MIVarCreate v) {
		super(obj, v);
	}

	public LocalVariable(Target target, Thread thread, StackFrame frame, String n, String q,
			int pos, int depth, MIVar miVar) {
		super(target, thread, frame, n, q, pos, depth, miVar);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.Variable#createVariable(org.eclipse.cdt.debug.mi.core.cdi.model.Target, java.lang.String, java.lang.String, org.eclipse.cdt.debug.core.cdi.model.ICDIThread, org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame, int, int, org.eclipse.cdt.debug.mi.core.output.MIVar)
	 */
	protected Variable createVariable(Target target, Thread thread, StackFrame frame, String name,
			String fullName, int pos, int depth, MIVar miVar) {
		return new LocalVariable(target, thread, frame, name, fullName, pos, depth, miVar);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.Variable#createDescriptor(org.eclipse.cdt.debug.mi.core.cdi.model.Target, org.eclipse.cdt.debug.mi.core.cdi.model.Thread, org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame, java.lang.String, java.lang.String, int, int)
	 */
	protected VariableDescriptor createDescriptor(Target target, Thread thread, StackFrame frame, String n, String fn, int pos, int depth) {
		return new LocalVariableDescriptor(target, thread, frame, n, fn, pos, depth);
	}
}
