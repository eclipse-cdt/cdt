/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;



/**
 * 
 *    signal SIGUSR1
 *
 */
public class MISignal extends CLICommand {

	public MISignal(String arg) {
		super("signal " + arg);
	}

}
