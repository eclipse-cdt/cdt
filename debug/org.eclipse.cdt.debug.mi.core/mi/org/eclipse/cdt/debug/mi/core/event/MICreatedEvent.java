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
public abstract class MICreatedEvent extends MIEvent {
	public MICreatedEvent(int id) {
		super(id);
	}
}
