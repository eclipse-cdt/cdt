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
public class MIThreadExitEvent extends MIDestroyedEvent {

	int tid;

	public MIThreadExitEvent(int id) {
		this(0, id);
	}

	public MIThreadExitEvent(int token, int id) {
		super(token);
		tid = id;
	}

	public int getId() {
		return tid;
	}
}
