/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *     -exec-return
 *
 *  Makes current function return immediately.  Doesn't execute the
 *  inferior.  Displays the new current frame.
 * 
 */
public class MIExecReturn extends MICommand 
{
	public MIExecReturn() {
		super("-exec-return");
	}
}
