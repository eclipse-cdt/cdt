/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
