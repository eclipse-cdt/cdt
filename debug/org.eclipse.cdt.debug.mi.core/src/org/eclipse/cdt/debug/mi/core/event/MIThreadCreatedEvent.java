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
public class MIThreadCreatedEvent extends MICreatedEvent {

	int tid;

	public MIThreadCreatedEvent(int id) {
		this(0, id);
	}

	public MIThreadCreatedEvent(int token, int id) {
		super(token);
		tid = id;
	}

	public int getId() {
		return tid;
	}
}
