/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIStackListLocalsInfo;

/**
 * 
 *     -stack-list-locals PRINT-VALUES
 *
 *  Display the local variable names for the current frame.  With an
 * argument of 0 prints only the names of the variables, with argument of 1
 * prints also their values.
 * 
 */
public class MIStackListLocals extends MICommand 
{
	public MIStackListLocals(boolean printValues) {
		super("-stack-list-locals");
		if (printValues) {
			setParameters(new String[]{"1"});
		} else {
			setParameters(new String[]{"0"});
		}
	}

	public MIStackListLocalsInfo getMIStackListLocalsInfo() throws MIException {
		return (MIStackListLocalsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIStackListLocalsInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
