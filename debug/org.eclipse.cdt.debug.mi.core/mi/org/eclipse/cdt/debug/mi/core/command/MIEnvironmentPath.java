/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -environment-path ( PATHDIR )+
 *
 *   Add directories PATHDIR to beginning of search path for object files.
 * 
 */
public class MIEnvironmentPath extends MICommand 
{
	public MIEnvironmentPath(String[] paths) {
		super("-environment-path", paths); //$NON-NLS-1$
	}
}
