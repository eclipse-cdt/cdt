/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MISharedLibChangedEvent extends MIChangedEvent {

	String filename;

	public MISharedLibChangedEvent(String name) {
		this(0, name);
	}

	public MISharedLibChangedEvent(int id, String name) {
		super(id);
		filename = name;
	}

	public String getName() {
		return filename;
	}

}
