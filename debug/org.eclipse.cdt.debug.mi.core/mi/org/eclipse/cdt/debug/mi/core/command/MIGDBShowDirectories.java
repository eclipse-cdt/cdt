/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -gdb-show directories
 *
 *   Show the current value of a GDB variable(directories).
 * 
 */
public class MIGDBShowDirectories extends MIGDBShow {
	public MIGDBShowDirectories() {
		super(new String[] { "directories" }); //$NON-NLS-1$
	}

	public MIGDBShowDirectoriesInfo getMIGDBShowDirectoriesInfo() throws MIException {
		return (MIGDBShowDirectoriesInfo)getMIInfo();
	}
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowDirectoriesInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
