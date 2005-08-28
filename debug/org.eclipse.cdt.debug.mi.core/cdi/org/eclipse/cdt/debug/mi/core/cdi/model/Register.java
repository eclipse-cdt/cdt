/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;


import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Register extends Variable implements ICDIRegister {

	/**
	 * @param target
	 * @param thread
	 * @param frame
	 * @param n
	 * @param q
	 * @param pos
	 * @param depth
	 * @param v
	 */
	public Register(Target target, Thread thread, StackFrame frame,
			String n, String q, int pos, int depth, MIVar miVar) {
		super(target, thread, frame, n, q, pos, depth, miVar);
	}

	public Register(RegisterDescriptor obj, MIVarCreate var) {
		super(obj, var);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.VariableDescriptor#getFullName()
	 */
	public String getFullName() {
		if (fFullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fFullName = "$" + n; //$NON-NLS-1$
			} else {
				fFullName = n;
			}
		}
		return fFullName;
	}

	protected Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, MIVar miVar) {
		return new Register(target, thread, frame, name, fullName, pos, depth, miVar);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#dispose()
	 */
	public void dispose() throws CDIException {
		ICDITarget target = getTarget();
		RegisterManager regMgr = ((Session)target.getSession()).getRegisterManager();
		regMgr.destroyRegister(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegister#getValue(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIValue getValue(ICDIStackFrame context) throws CDIException {
		Session session = (Session)getTarget().getSession();
		RegisterManager mgr = session.getRegisterManager();
		Variable var = mgr.createShadowRegister(this, (StackFrame)context, getQualifiedName());
		return var.getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegister#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIRegister)
	 */
	public boolean equals(ICDIRegister register) {
		if (register instanceof Register) {
			Register reg = (Register) register;
			return super.equals(reg);
		}
		return super.equals(register);
	}

}
