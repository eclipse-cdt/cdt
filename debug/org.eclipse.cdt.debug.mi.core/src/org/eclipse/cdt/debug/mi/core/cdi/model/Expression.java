/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Expression extends Variable implements ICDIExpression {

	public Expression(VariableObject obj, MIVar var) {
		super(obj, var);
	}
}
