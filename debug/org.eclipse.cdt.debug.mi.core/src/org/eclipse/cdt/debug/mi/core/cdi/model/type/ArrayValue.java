/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

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
		int timeout = variable.getMIVar().getNumChild() * 8 + 5000;

		return variable.getChildren(timeout);
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

		//String subarray = "*(" + variable.getName() + "+" + index + ")@" + length;
		ICDITarget target = getTarget();
		Session session = (Session) (target.getSession());
		ICDIVariableManager mgr = session.getVariableManager();
		ICDIVariableObject vo = mgr.getVariableObjectAsArray(variable, variable.getTypeName(), index, length);
		return mgr.createVariable(vo).getValue().getVariables();
	}
}
