/*******************************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;


/*
 * This inteface serves as an address factory. If you need to 
 * implement your own addresses, you should extend this.
 * 
 * Please see Addr32Factory and Addr64Factory to see how it can be implemented.
 */
public interface IAddressFactory 
{
	/*
	 * Returns zero address, i.e. minimal possible address
	 */
	IAddress getZero();
	/*
	 * Returns maximal address.
	 */
	IAddress getMax();
	/*
	 * Creates address from string representation. 
	 * 
	 * 1. This method should be able to create address from hex 
	 *    address string (string produced with 
	 *    IAddress.toHexAddressString() method). 
	 * 2. Method should be case insensetive
	 * 3. Method should be able to create address from decimal address 
	 *    representation
	 * 
	 *   Please see Addr32Factory.createAddress() for reference implementation.
	 */
	IAddress createAddress(String addr);
	/*
	 * Creates address from string with given radix. 
	 * 
	 * Given string should not contain any prefixes or sign numbers.
	 * 
	 * Method should be case insensetive
	 */
	IAddress createAddress(String addr, int radix);
}
