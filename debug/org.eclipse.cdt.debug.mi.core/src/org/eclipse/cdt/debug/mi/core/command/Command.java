/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 * A base class for all mi requests.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public abstract class Command
{
	/**
	 * Returns the identifier of this request.
	 * 
	 * @return the identifier of this request
	 */
	public abstract int getToken();
	
	public abstract void setToken(int token);

	public abstract String toString();

	public abstract void setMIOutput(MIOutput mi);

	public abstract MIInfo getInfo();
}
