/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -file-exec-file FILE
 *
 *  Specify the executable file to be debugged.  Unlike
 * `-file-exec-and-symbols', the symbol table is _not_ read from this
 * file.  If used without argument, GDB clears the information about the
 * executable file.  No output is produced, except a completion
 * notification.
 * 
 */
public class MIFileExecFile extends MICommand 
{
	public MIFileExecFile(String file) {
		super("-file-exec-file", new String[]{file});
	}
}
