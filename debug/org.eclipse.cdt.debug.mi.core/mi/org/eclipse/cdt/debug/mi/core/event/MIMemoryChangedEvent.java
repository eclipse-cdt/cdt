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

import org.eclipse.cdt.core.IAddress;

/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIMemoryChangedEvent extends MIChangedEvent {

	IAddress[] addresses;

	public MIMemoryChangedEvent(MISession source, IAddress[] addrs) {
		this(source, 0, addrs);
	}

	public MIMemoryChangedEvent(MISession source, int token, IAddress[] addrs) {
		super(source, token);
		addresses = addrs;
	}

	public IAddress[] getAddresses() {
		return addresses;
	}
}
