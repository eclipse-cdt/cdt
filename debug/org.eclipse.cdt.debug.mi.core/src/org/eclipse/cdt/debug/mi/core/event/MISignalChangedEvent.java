/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MISignalChangedEvent extends MIChangedEvent {

	String name;

	public MISignalChangedEvent(String n) {
		this(0, n);
	}

	public MISignalChangedEvent(int id, String n) {
		super(id);
		name = n;
	}

	public String getName() {
		return name;
	}

}
