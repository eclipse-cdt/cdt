/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 *  ^running
 */
public class MIDetachedEvent extends MIEvent {

	public MIDetachedEvent(int token) {
		super(token);
	}

	public String toString() {
		return "Detached";
	}
}
