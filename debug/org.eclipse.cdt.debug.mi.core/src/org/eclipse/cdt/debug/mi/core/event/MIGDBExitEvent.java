/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 * Gdb Session terminated.
 */
public class MIGDBExitEvent extends MIEvent {

	public MIGDBExitEvent(int token) {
		super(token);
	}
}
