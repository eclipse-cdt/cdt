/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MIBreakpointChangedEvent extends MIChangedEvent {

	int no;

	public MIBreakpointChangedEvent(int number) {
		this(0, number);
	}

	public MIBreakpointChangedEvent(int id, int number) {
		super(id);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
