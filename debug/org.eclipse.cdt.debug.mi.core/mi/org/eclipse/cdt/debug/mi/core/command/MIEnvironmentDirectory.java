/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -environment-directory PATHDIR
 *
 *   Add directory PATHDIR to beginning of search path for source files.
 * 
 */
public class MIEnvironmentDirectory extends MICommand 
{
	public MIEnvironmentDirectory(String[] paths) {
		super("-environment-directory", paths); //$NON-NLS-1$
	}

}
