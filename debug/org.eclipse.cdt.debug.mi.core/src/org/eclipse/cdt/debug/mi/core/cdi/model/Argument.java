/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Argument extends Variable implements ICDIArgument {

	public Argument(StackFrame frame, String name, MIVar var) {
		super(frame, name, var);
	}
}
