/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -environment-pwd
 *
 *   Show the current working directory.
 * 
 */
public class MIEnvironmentPWD extends MICommand 
{
	public MIEnvironmentPWD() {
		super("-environment-pwd");
	}
}
