/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    sharedlibrary regex
 *
 */
public class MISharedLibrary extends CLICommand
{
	public MISharedLibrary(String lib) {
		super("sharedlibrary " + lib);
	}

}
