/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -exec-until [ LOCATION ]
 *
 *  Asynchronous command.  Executes the inferior until the LOCATION
 * specified in the argument is reached.  If there is no argument, the
 * inferior executes until a source line greater than the current one is
 * reached.  The reason for stopping in this case will be
 * `location-reached'.
 * 
 */
public class MIExecUntil extends MICommand 
{
	public MIExecUntil() {
		super("-exec-until"); //$NON-NLS-1$
	}

	public MIExecUntil(String loc) {
		super("-exec-until", new String[]{loc}); //$NON-NLS-1$
	}
}
