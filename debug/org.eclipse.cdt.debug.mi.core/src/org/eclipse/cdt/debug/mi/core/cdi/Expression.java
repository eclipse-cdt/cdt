/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Expression extends Variable implements ICDIExpression {

	public Expression(StackFrame stackframe, String name, MIVar var) {
		super(stackframe, name, var);
	}
}
