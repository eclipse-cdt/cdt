/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -file-symbol-file FILE
 *
 *  Read symbol table info from the specified FILE argument.  When used
 * without arguments, clears GDB's symbol table info.  No output is
 * produced, except for a completion notification.
 * 
 */
public class MIFileSymbolFile extends MICommand 
{
	public MIFileSymbolFile(String file) {
		super("-file-symbol-file", new String[]{file}); //$NON-NLS-1$
	}
}
