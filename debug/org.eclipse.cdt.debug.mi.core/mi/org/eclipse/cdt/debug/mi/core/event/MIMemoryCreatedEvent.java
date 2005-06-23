/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.event;

import java.math.BigInteger;

import org.eclipse.cdt.debug.mi.core.MISession;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIMemoryCreatedEvent extends MICreatedEvent {

	BigInteger address;
	long totalBytes;

	public MIMemoryCreatedEvent(MISession source, BigInteger addr, long total) {
		this(source, 0, addr, total);
	}

	public MIMemoryCreatedEvent(MISession source, int token, BigInteger addr, long total) {
		super(source, token);
		address = addr;
		totalBytes = total;
	}

	public BigInteger getAddress() {
		return address;
	}

	public long getLength() {
		return totalBytes;
	}

}
