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
public class MIMemoryCreatedEvent extends MICreatedEvent {

	long address;
	long totalBytes;

	public MIMemoryCreatedEvent(long addr, long total) {
		this(0, addr, total);
	}

	public MIMemoryCreatedEvent(int token, long addr, long total) {
		super(token);
		address = addr;
		totalBytes = total;
	}

	public long getAddress() {
		return address;
	}

	public long getLength() {
		return totalBytes;
	}

}
