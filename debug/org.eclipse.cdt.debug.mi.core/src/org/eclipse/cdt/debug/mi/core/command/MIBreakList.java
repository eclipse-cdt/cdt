/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *   -break-list
 *
 *   Displays the list of inserted breakpoints, showing the following
 * fields:
 *
 * `Number'
 *     number of the breakpoint
 *
 * `Type'
 *     type of the breakpoint: `breakpoint' or `watchpoint'
 *
 * `Disposition'
 *     should the breakpoint be deleted or disabled when it is hit: `keep'
 *     or `nokeep'
 *
 * `Enabled'
 *     is the breakpoint enabled or no: `y' or `n'
 *
 * `Address'
 *     memory location at which the breakpoint is set
 *
 * `What'
 *     logical location of the breakpoint, expressed by function name,
 *
 * `Times'
 *     number of times the breakpoint has been hit
 *
 *   If there are no breakpoints or watchpoints, the `BreakpointTable'
 *   `body' field is an empty list.
 *
 */
public class MIBreakList extends MICommand
{
	public MIBreakList () {
		super("-break-list");
	}
}
