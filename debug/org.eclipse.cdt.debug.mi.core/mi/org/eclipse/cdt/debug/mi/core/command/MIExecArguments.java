/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -exec-arguments ARGS
 *
 *   Set the inferior program arguments, to be used in the next
 *  `-exec-run'.
 * 
 */
public class MIExecArguments extends MICommand 
{
	public MIExecArguments(String[] args) {
		super("-exec-arguments", args);
	}
}
