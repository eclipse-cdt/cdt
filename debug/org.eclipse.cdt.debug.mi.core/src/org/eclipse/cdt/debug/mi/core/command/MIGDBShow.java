/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -gdb-show
 *
 *   Show the current value of a GDB variable.
 * 
 */
public class MIGDBShow extends MICommand {
	public MIGDBShow(String[] params) {
		super("-gdb-show", params);
	}

	public MIGDBShowInfo getMIGDBShowInfo() throws MIException {
		return (MIGDBShowInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
