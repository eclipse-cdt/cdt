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
public class MIMemoryChangedEvent extends MIChangedEvent {

	Long[] addresses;
	public MIMemoryChangedEvent(Long[] addrs) {
		addresses = addrs;
	}

	public Long[] getAddresses() {
		return addresses;
	}
}
