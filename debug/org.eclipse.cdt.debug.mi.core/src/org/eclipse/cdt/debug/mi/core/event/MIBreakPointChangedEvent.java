/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MIBreakPointChangedEvent extends MIChangedEvent {

	int no;

	public MIBreakPointChangedEvent(int number) {
		super(0);
		no = number;
	}

	public MIBreakPointChangedEvent(int id, int number) {
		super(id);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
