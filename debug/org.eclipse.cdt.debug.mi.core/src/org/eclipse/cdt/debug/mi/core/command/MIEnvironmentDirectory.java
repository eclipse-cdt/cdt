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
	public MIEnvironmentDirectory(String path) {
		super("-environment-directory", new String[]{path});
	}
}
