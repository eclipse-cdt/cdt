/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIStackInfoDepthInfo;

/**
 * 
 *     -stack-info-depth [ MAX-DEPTH ]
 *
 *  Return the depth of the stack.  If the integer argument MAX-DEPTH is
 *  specified, do not count beyond MAX-DEPTH frames.
 * 
 */
public class MIStackInfoDepth extends MICommand 
{
	public MIStackInfoDepth() {
		super("-stack-info-depth");
	}

	public MIStackInfoDepth(int maxDepth) {
		super("-stack-info-depth", new String[]{Integer.toString(maxDepth)});
	}

	public MIStackInfoDepthInfo getMIStackInfoDepthInfo() throws MIException {
		return (MIStackInfoDepthInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIStackInfoDepthInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
