/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSignalsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *    info threads
 *
 */
public class MIInfoSignals extends CLICommand 
{
	public MIInfoSignals() {
		super("info signals");
	}

	public MIInfoSignals(String name) {
		super("info signal " + name);
	}

	public MIInfoSignalsInfo getMIInfoSignalsInfo() throws MIException {
		return (MIInfoSignalsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoSignalsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
