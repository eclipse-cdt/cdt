/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MIBreakPointCreatedEvent extends MICreatedEvent {

	int no;

	public MIBreakPointCreatedEvent(int number) {
		super(0);
		no = number;
	}

	public MIBreakPointCreatedEvent(int id, int number) {
		super(id);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
