/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI shared information
 */
public class MIShared {

	long from;
	long to;
	boolean isread;
	String name;

	public MIShared (long start, long end, boolean read, String location) {
		from = start;
		to = end;
		isread = read;
		name = location;
	}

	public long getFrom() {
		return from;
	}

	public long getTo() {
		return to;
	}

	public boolean isRead() {
		return isread;
	}

	public String getName() {
		return name;
	}

	public void setSymbolsRead(boolean read) {
		isread = read;
	}

}
