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
 *     -stack-select-frame FRAMENUM
 *
 *  Change the current frame.  Select a different frame FRAMENUM on the
 * stack.
 * 
 */
public class MIStackSelectFrame extends MICommand 
{
	public MIStackSelectFrame(int frameNum) {
		super("-stack-select-frame", new String[]{Integer.toString(frameNum)});
	}
}
