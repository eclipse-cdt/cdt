/*******************************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.math.BigInteger;

/**
 * This inteface serves as an address factory. If you need to 
 * implement your own addresses, you should extend this.
 * 
 * Please see Addr32Factory and Addr64Factory to see how it can be implemented.
 */
public interface IAddressFactory 
{
	/**
	 * Returns zero address, i.e. minimal possible address
	 */
	IAddress getZero();

	/**
	 * Returns maximal address.
	 */
	IAddress getMax();

	/**
	 * Creates address from string representation. 
	 * 
	 * 1. Method should be able to create address from hex 
	 *    address string (string produced with 
	 *    IAddress.toHexAddressString() method). 
	 * 2. Method should be case insensetive
	 * 3. Method should be able to create address from decimal address 
	 *    representation
	 * 4. Method should throw NumberFormatException if the given string
	 *    cannot be decoded.
	 * 5. Method should not attempt to evaluate string as expression (i.e., 
	 *    "0x1000 + 5" should not result in an IAddress for 0x1005.) Input
	 *    must be a straightforward, absolute value.
	 * 
	 * Please see Addr32Factory.createAddress() for reference implementation.
	 *
	 * @param addr
	 */
	IAddress createAddress(String addr);

	/**
	 * Creates address from string with given radix. 
	 * 
	 * Given string should not contain any prefixes or sign numbers.
	 * 
	 * Method should be case insensetive
	 * 
	 * Method should throw NumberFormatException if the given string
	 * cannot be decoded.
	 * 
	 * Method should not attempt to evaluate string as expression (i.e., 
	 * "1000 + 5" should not result in an IAddress for 1005.) Input
	 * must be a straightforward, absolute value.
	 * 
	 * @param addr
	 * @param radix
	 */
	IAddress createAddress(String addr, int radix);
	
	/**
	 * Create address from a BigInteger
	 * 
	 * @param addr
	 */
	IAddress createAddress(BigInteger addr);
}
