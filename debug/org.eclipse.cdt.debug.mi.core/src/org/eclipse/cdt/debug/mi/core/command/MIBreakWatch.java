/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -break-watch [ -a | -r ]
 *
 * Create a watchpoint.  With the `-a' option it will create an
 * "access" watchpoint, i.e. a watchpoint that triggers either on a read
 * from or on a write to the memory location.  With the `-r' option, the
 * watchpoint created is a "read" watchpoint, i.e. it will trigger only
 * when the memory location is accessed for reading.  Without either of
 * the options, the watchpoint created is a regular watchpoint, i.e. it
 * will trigger when the memory location is accessed for writing. 
 * 
 */
public class MIBreakWatch extends MICommand
{
	public MIBreakWatch (boolean access, boolean read, String expr) {
		super("-break-watch");
		String[] opts = null;
		if (access) {
			opts = new String[] {"-a"};
		} else if (read) {
			opts = new String[] {"-r"};
		}
		if (opts != null) {
			setOptions(opts);
		}			
		setParameters(new String[]{expr});
	}
}
