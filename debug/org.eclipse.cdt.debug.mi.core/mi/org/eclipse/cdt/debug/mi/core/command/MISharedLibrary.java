/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;



/**
 * 
 *    sharedlibrary filename
 *
 */
public class MISharedLibrary extends CLICommand {

	public MISharedLibrary() {
		super("sharedlibrary");
	}

	public MISharedLibrary(String name) {
		super("sharedlibrary " + name);
	}
}
