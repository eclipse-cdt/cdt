/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -gdb-show
 *
 *   Show the current value of a GDB variable.
 * 
 */
public class MIGDBShowExitCode extends MIGDBShow {
	public MIGDBShowExitCode() {
		super(new String[] { "convenience", "$_exitcode" });
	}

	public MIGDBShowExitCodeInfo getMIGDBShowExitCodeInfo() throws MIException {
		return (MIGDBShowExitCodeInfo)getMIInfo();
	}
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowExitCodeInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
