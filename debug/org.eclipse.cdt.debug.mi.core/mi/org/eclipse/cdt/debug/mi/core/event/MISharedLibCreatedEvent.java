/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 */
public class MISharedLibCreatedEvent extends MICreatedEvent {

	String filename;

	public MISharedLibCreatedEvent(String name) {
		this(0, name);
	}

	public MISharedLibCreatedEvent(int id, String name) {
		super(id);
		filename = name;
	}

	public String getName() {
		return filename;
	}

}
