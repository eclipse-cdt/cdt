/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -stack-list-locals PRINT-VALUES
 *
 *  Display the local variable names for the current frame.  With an
 * argument of 0 prints only the names of the variables, with argument of 1
 * prints also their values.
 * 
 */
public class MIStackListLocals extends MICommand 
{
	public MIStackListLocals(boolean printValues) {
		super("-stack-list-locals");
		if (printValues) {
			setParameters(new String[]{"1"});
		} else {
			setParameters(new String[]{"0"});
		}
	}
}
