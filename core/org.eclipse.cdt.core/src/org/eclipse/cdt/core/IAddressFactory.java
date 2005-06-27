/*******************************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
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
	 * @return
	 */
	IAddress getZero();

	/**
	 * Returns maximal address.
	 * @return
	 */
	IAddress getMax();

	/**
	 * Creates address from string representation. 
	 * 
	 * 1. This method should be able to create address from hex 
	 *    address string (string produced with 
	 *    IAddress.toHexAddressString() method). 
	 * 2. Method should be case insensetive
	 * 3. Method should be able to create address from decimal address 
	 *    representation
	 * 
	 * Please see Addr32Factory.createAddress() for reference implementation.
	 *
	 * @param addr
	 * @return
	 */
	IAddress createAddress(String addr);

	/**
	 * Creates address from string with given radix. 
	 * 
	 * Given string should not contain any prefixes or sign numbers.
	 * 
	 * Method should be case insensetive
	 * 
	 * @param addr
	 * @param radix
	 * @return
	 */
	IAddress createAddress(String addr, int radix);
	
	/**
	 * Create address from a BigInteger
	 * 
	 * @param addr
	 * @return
	 */
	IAddress createAddress(BigInteger addr);
}
