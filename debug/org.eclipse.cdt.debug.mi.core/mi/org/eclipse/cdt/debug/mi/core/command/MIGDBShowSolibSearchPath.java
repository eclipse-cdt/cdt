/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowSolibSearchPathInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -gdb-show directories
 *
 *   Show the current value of a GDB variable(directories).
 * 
 */
public class MIGDBShowSolibSearchPath extends MIGDBShow {
	public MIGDBShowSolibSearchPath() {
		super(new String[] { "solib-search-path" }); //$NON-NLS-1$
	}

	public MIGDBShowSolibSearchPathInfo getMIGDBShowSolibSearchPathInfo() throws MIException {
		return (MIGDBShowSolibSearchPathInfo)getMIInfo();
	}
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowSolibSearchPathInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
