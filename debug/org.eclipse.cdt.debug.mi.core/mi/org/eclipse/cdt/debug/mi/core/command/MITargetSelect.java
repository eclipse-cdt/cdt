/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -target-select TYPE PARAMETERS ...
 *
 *  Connect GDB to the remote target.  This command takes two args:
 *
 * `TYPE'
 *     The type of target, for instance `async', `remote', etc.
 *
 * `PARAMETERS'
 *   Device names, host names and the like.  *Note Commands for
 *   managing targets: Target Commands, for more details.
 *
 * The output is a connection notification, followed by the address at
 * which the target program is, in the following form:
 *
 *   ^connected,addr="ADDRESS",func="FUNCTION NAME",
 *     args=[ARG LIST]
 * 
 */
public class MITargetSelect extends MICommand 
{
	public MITargetSelect(String[] params) {
		super("-target-select", params); //$NON-NLS-1$
	}
}
