/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *    info threads
 *
 */
public class MIInfoThreads extends CLICommand 
{
	public MIInfoThreads() {
		super("info threads");
	}

	public MIInfoThreadsInfo getMIInfoThreadsInfo() throws MIException {
		return (MIInfoThreadsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoThreadsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
