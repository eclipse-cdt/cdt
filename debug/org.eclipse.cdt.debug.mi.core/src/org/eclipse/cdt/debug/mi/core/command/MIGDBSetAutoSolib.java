/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -gdb-set
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSetAutoSolib extends MIGDBSet {
	public MIGDBSetAutoSolib(boolean isSet) {
		super(new String[] {"auto-solib-add", (isSet) ? "1" : "0"});
	}
}
