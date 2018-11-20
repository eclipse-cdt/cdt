/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import java.math.BigInteger;

public class Addr2Line {
	public BigInteger addr;
	public Addr2Line next;
	public int first;
	public int last;

	public static int hash(BigInteger addr, int size) {
		return (int) ((addr.shiftRight(2).longValue()) % size);
	}
}
