/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *
 *      -environment-cd PATHDIR
 *
 *   Set GDB's working directory.
 *
 * 
 */
public class MIEnvironmentCD extends MICommand 
{
	public MIEnvironmentCD(String path) {
		super("-environment-cd", new String[]{path}); //$NON-NLS-1$
	}

	/**
	 * !@*^%^$( sigh ... gdb for this command does not make any interpretation
	 * So we must past the command verbatim without any changes. 
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#parametersToString()
	 */
	protected String parametersToString() {
		if (parameters != null && parameters.length == 1) {
			return parameters[0];
		}
		return super.parametersToString();		
	}
}
