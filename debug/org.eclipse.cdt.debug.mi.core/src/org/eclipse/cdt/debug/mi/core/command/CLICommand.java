/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 * 
 * Represents a CLI command.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public class CLICommand extends Command
{
	/**
	 * Returns the text representation of this command.
	 * 
	 * @return the text representation of this command
	 */
	public String getToken() {
		return "";
	}
	
	public String toString(){
		return "";
	}

	public MIInfo getInfo (MIResultRecord rr) {
		return new MIInfo(rr);
	}
}
