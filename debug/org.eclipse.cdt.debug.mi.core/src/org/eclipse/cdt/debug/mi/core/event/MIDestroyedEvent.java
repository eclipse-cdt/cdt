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
public abstract class MIDestroyedEvent extends MIEvent {
	public MIDestroyedEvent(int id) {
		super(id);
	}
}
