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
}
