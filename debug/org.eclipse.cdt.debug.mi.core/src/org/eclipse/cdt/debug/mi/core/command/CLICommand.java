/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 * Represents a CLI command.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public class CLICommand extends Command
{
	String token = "";
	MIOutput miOutput = null;
	
	/**
	 * Returns the text representation of this command.
	 * 
	 * @return the text representation of this command
	 */
	public String getToken() {
		return token;
	}
	
	public void setToken(String t) {
		token = t;
	}
	
	public String toString(){
		return "";
	}

	public void setMIOutput(MIOutput mi) {
		miOutput = mi;
	}

	public MIInfo getInfo () {
		return null;
	}
}
