/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -stack-list-frames [ LOW-FRAME HIGH-FRAME ]
 *
 *  List the frames currently on the stack.  For each frame it displays
 * the following info:
 *
 * `LEVEL'
 *    The frame number, 0 being the topmost frame, i.e. the innermost
 *    function.
 *
 * `ADDR'
 *    The `$pc' value for that frame.
 *
 * `FUNC'
 *    Function name.
 *
 * `FILE'
 *    File name of the source file where the function lives.
 *
 * `LINE'
 *   Line number corresponding to the `$pc'.
 *
 *  If invoked without arguments, this command prints a backtrace for the
 * whole stack.  If given two integer arguments, it shows the frames whose
 * levels are between the two arguments (inclusive).  If the two arguments
 * are equal, it shows the single frame at the corresponding level.
 * 
 */
public class MIStackListFrames extends MICommand 
{
	public MIStackListFrames() {
		super("-stack-list-frames");
	}

	public MIStackListFrames(int low, int high) {
		super("-stack-list-frames", new String[]{Integer.toString(low), Integer.toString(high)});
	}
}
