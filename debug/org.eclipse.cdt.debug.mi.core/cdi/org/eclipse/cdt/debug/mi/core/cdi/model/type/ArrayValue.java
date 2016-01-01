/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerValue;
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
public class ArrayValue extends DerivedValue implements ICDIArrayValue, ICDIPointerValue {

	private String hexAddress;
	
	/**
	 * Construct the array value object given a variable
	 * 
	 * @param v
	 * @since 7.1
	 */
	public ArrayValue(Variable v) {
		super(v);
	}

	/**
	 * Construct the array value object given a variable and the
	 * hexadecimal address of the variable.
	 * 
	 * @param v
	 * @param hexAddress
	 */
	public ArrayValue(Variable v, String address) {
		this(v);
		hexAddress = address;
	}

	/**
	 * Compute array address as string.
	 */
	private String getAddressString() throws CDIException {
		if (hexAddress != null) 
			return hexAddress;
		
		String address = getVariable().getHexAddress();
		if (address == null) {
			address = ""; //$NON-NLS-1$
		}
		if (address.startsWith("0x") || address.startsWith("0X")) { //$NON-NLS-1$ //$NON-NLS-2$
			hexAddress = address.substring(2);
		} else {
			hexAddress = address;
		}
		return hexAddress;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	@Override
    public ICDIVariable[] getVariables() throws CDIException {

		/* GDB is appallingly slow on array fetches. As as slow as 128 entries
		 * per second on NT gdbs with slow processors. We need to set a timeout
		 * that's appropriately scaled by number of children to give the slave
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
	@Override
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerValue#pointerValue()
	 */
	@Override
	public BigInteger pointerValue() throws CDIException {
		String address = getAddressString();
		if (address.length() > 0 ){
			try {
				return new BigInteger(address, 16);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
}
