/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
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
	int token = 0;
	MIOutput output;

	/**
	 * Returns the identifier of this request.
	 * 
	 * @return the identifier of this request
	 */
	public int getToken() {
		return token;
	}
	
	public void setToken(int token) {
		this.token = token;
	}

	public MIOutput getMIOutput() {
		return output;
	}

	public void setMIOutput(MIOutput mi) {
		output = mi;
	}

	/**
	 * Parse the MIOutput generate after posting the command.
	 */
	public MIInfo getMIInfo () throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfo(out);
			if (info.isError()) {
				String s = info.getErrorMsg();
				throw new MIException(s);
			}
		}
		return info;
	}
}
