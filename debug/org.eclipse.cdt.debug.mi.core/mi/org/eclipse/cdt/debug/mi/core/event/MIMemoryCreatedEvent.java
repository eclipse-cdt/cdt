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
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIMemoryCreatedEvent extends MICreatedEvent {

	long address;
	long totalBytes;

	public MIMemoryCreatedEvent(MISession source, long addr, long total) {
		this(source, 0, addr, total);
	}

	public MIMemoryCreatedEvent(MISession source, int token, long addr, long total) {
		super(source, token);
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
