/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;


/**
 * 
 *    jump LINESPEC
 *
 */
public class MIJump extends CLICommand {

	public MIJump(String loc) {
		super("jump " + loc);
	}

}
