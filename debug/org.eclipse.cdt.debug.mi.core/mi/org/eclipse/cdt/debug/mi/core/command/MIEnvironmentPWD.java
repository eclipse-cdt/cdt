/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIEnvironmentPWDInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

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
		super("-environment-pwd"); //$NON-NLS-1$
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIEnvironmentPWDInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
        }

}
