/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIThreadListIdsInfo;

/**
 * 
 *    -thread-list-ids
 *
 * Produces a list of the currently known GDB thread ids.  At the end
 * of the list it also prints the total number of such threads.
 * 
 */
public class MIThreadListIds extends MICommand 
{
	public MIThreadListIds() {
		super("-thread-list-ids");
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIThreadListIdsInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
