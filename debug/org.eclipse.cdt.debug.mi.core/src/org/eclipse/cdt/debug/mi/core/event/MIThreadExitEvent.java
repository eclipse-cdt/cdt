/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;


/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIThreadExitEvent extends MIEvent {

	int tid;

	public MIThreadExitEvent(int id) {
		tid = id;
	}

	public int getId() {
		return tid;
	}
}
