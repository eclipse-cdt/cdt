/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 * Represents a MI command.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public class MICommand extends Command
{
	/**
	 * Returns the operation of this command.
	 * 
	 * @return the operation of this command
	 */
	public String getOperation() {
		return "";
	}
	
	/**
	 * Returns an array of command's options. An empty collection is 
	 * returned if there are no options.
	 * 
	 * @return an array of command's options
	 */
	public String[] getOptions() {
		return new String[0];
	}
	
	/**
	 * Returns an array of command's parameters. An empty collection is 
	 * returned if there are no parameters.
	 * 
	 * @return an array of command's parameters
	 */
	public String[] getParameters() {
		return new String[0];
	}

	public String toString() {
		return "";
	}

	public String getToken() {
		return "";
	}
}
