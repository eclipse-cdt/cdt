/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -var-assign NAME EXPRESSION
 *
 *  Assigns the value of EXPRESSION to the variable object specified by
 * NAME.  The object must be `editable'.
 * 
 */
public class MIVarAssign extends MICommand 
{
	public MIVarAssign(String name, String expression) {
		super("-var-assign", new String[]{name, expression}); //$NON-NLS-1$
	}
}
