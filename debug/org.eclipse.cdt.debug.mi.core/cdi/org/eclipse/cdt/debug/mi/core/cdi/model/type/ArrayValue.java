/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.cdi.model.VariableDescriptor;

/**
 * Enter type comment.
 * 
 * @since Jun 3, 2003
 */
public class ArrayValue extends DerivedValue implements ICDIArrayValue {

	public ArrayValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public ICDIVariable[] getVariables() throws CDIException {

		/* GDB is appallingly slow on array fetches. As as slow as 128 entries
		 * per second on NT gdbs with slow processors. We need to set a timeout
		 * that's appropraitely scaled by number of children to give the slave
		 * GDB time to respond. In the end perhaps we want a UI for this. As it 
		 * is, let's just make up a number that's 5 seconds for us plus one 
		 * second for every 128 entries. */
		int timeout = getVariable().getMIVar().getNumChild() * 8 + 5000;

		return getVariable().getChildren(timeout);
	}

	/**
	 * 
	 * an Array of range[index, index + length - 1]
	 */
	public ICDIVariable[] getVariables(int index, int length) throws CDIException {
		//int children = getChildrenNumber();
		//if (index >= children || index + length >= children) {
		//	throw new CDIException("Index out of bound");
		//}

		// Overload for registers.
		Variable variable = getVariable();
		if (variable instanceof Register) {
			ICDIVariable[] vars = getVariables();
			
			if (index < vars.length && (index + length) <= vars.length) {
				ICDIVariable[] newVars = new ICDIVariable[length];
				System.arraycopy(vars, index, newVars, 0, length);
				return newVars;
			}
			return new ICDIVariable[0];
		}
		//String subarray = "*(" + variable.getName() + "+" + index + ")@" + length;
		ICDITarget target = getTarget();
		Session session = (Session) (target.getSession());
		VariableManager mgr = session.getVariableManager();
		ICDIVariableDescriptor vo = mgr.getVariableDescriptorAsArray(variable, index, length);
		return mgr.createVariable((VariableDescriptor)vo).getValue().getVariables();
	}
}
