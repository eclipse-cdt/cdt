/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Register extends Variable implements ICDIRegister {

	public Register(StackFrame frame, String name, MIVar var) {
		super(frame, name, var);
	}
}
