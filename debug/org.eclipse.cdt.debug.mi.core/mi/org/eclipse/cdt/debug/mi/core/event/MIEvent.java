/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

/**
 */
public abstract class MIEvent {

	int token;

	public MIEvent(int token) {
		this.token = token;
	}

	public int getToken() {
		return token;
	}
}
