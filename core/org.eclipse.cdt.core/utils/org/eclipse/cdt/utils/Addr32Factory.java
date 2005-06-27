/*******************************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;

public class Addr32Factory implements IAddressFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IAddressFactory#getZero()
	 */
	public IAddress getZero() {
		return Addr32.ZERO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IAddressFactory#getMax()
	 */
	public IAddress getMax() {
		return Addr32.MAX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IAddressFactory#createAddress(java.lang.String)
	 */
	public IAddress createAddress(String addr) {
		IAddress address = new Addr32(addr);
		return address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IAddressFactory#createAddress(java.lang.String,
	 *      int)
	 */
	public IAddress createAddress(String addr, int radix) {
		IAddress address = new Addr32(addr, radix);
		return address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IAddressFactory#createAddress(java.math.BigInteger)
	 */
	public IAddress createAddress(BigInteger addr) {
		IAddress address = new Addr32(addr.longValue());
		return address;
	}
}