/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoProgramInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *    info threads
 *
 */
public class MIInfoProgram extends CLICommand 
{
	public MIInfoProgram() {
		super("info program");
	}

	public MIInfoProgramInfo getMIInfoProgramInfo() throws MIException {
		return (MIInfoProgramInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoProgramInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
