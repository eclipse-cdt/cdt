/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
public class MIMemoryChangedEvent extends MIChangedEvent {

	BigInteger[] addresses;

	public MIMemoryChangedEvent(MISession source, BigInteger[] addrs) {
		this(source, 0, addrs);
	}

	public MIMemoryChangedEvent(MISession source, int token, BigInteger[] addrs) {
		super(source, token);
		addresses = addrs;
	}

	public BigInteger[] getAddresses() {
		return addresses;
	}
}
