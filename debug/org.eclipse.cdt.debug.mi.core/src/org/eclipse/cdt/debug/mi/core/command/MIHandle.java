/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;



/**
 * 
 *    handle SIGUSR1 nostop noignore
 *
 */
public class MIHandle extends CLICommand {

	public MIHandle(String arg) {
		super("handle " + arg);
	}

}
