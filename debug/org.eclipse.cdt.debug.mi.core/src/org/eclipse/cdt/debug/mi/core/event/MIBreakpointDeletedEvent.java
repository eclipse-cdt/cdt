/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MIBreakpointDeletedEvent extends MIDestroyedEvent {

	int no;

	public MIBreakpointDeletedEvent(int number) {
		super(0);
		no = number;
	}

	public MIBreakpointDeletedEvent(int id, int number) {
		super(id);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
