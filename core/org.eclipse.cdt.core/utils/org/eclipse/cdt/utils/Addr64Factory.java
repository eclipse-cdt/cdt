/*******************************************************************************
 * Copyright (c) 2004, 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory2;

public class Addr64Factory implements IAddressFactory2 {

	@Override
	public IAddress getZero() {
		return Addr64.ZERO;
	}

	@Override
	public IAddress getMax() {
		return Addr64.MAX;
	}

	@Override
	public IAddress createAddress(String addr) {
		return createAddress(addr, true);
	}

	@Override
	public IAddress createAddress(String addr, boolean truncate) {
		return new Addr64(addr, truncate);
	}

	@Override
	public IAddress createAddress(String addr, int radix) {
		return createAddress(addr, radix, true);
	}

	@Override
	public IAddress createAddress(String addr, int radix, boolean truncate) {
		return new Addr64(addr, radix, truncate);
	}

	@Override
	public IAddress createAddress(BigInteger addr) {
		return createAddress(addr, true);
	}

	@Override
	public IAddress createAddress(BigInteger addr, boolean truncate) {
		return new Addr64(addr, truncate);
	}

	/**
	 * @since 5.9
	 */
	public IAddress createAddress(long addr) {
		return new Addr64(addr);
	}
}