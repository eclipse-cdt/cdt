/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 * 
 *   -break-condition NUMBER EXPR
 *
 * Breakpoint NUMBER will stop the program only if the condition in
 * EXPR is true.  The condition becomes part of the `-break-list' output
 * Result:
 *  ^done
 */
public class MIBreakCondition extends MICommand {
	public MIBreakCondition(int brknum, String expr) {
		super("-break-condition", new String[] { Integer.toString(brknum), expr });
	}

	/**
	 * Do not do any munging on the string i.e. quoting spaces
	 * etc .. doing this will break the command -break-condition.
	 */
	protected String parametersToString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < parameters.length; i++) {
			buffer.append(' ').append(parameters[i]);
		}
		return buffer.toString().trim();
	}
}
