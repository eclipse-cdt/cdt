/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

/**
 *
 */
public class MISharedLibUnloadedEvent extends MIDestroyedEvent {

	String filename;

	public MISharedLibUnloadedEvent(String name) {
		this(0, name);
	}

	public MISharedLibUnloadedEvent(int id, String name) {
		super(id);
		filename = name;
	}

	public String getName() {
		return filename;
	}

}
