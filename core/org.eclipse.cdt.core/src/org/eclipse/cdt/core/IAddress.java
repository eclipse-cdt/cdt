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

import java.math.BigInteger;

/*
 * Represents C/C++ address in CDT. All implementors of this inteface should be 
 * immutable, i.e. all methods should not modify objects, they should return 
 * new object.
 * 
 * Please see Addr32 and Addr64 classes to see how this interface should 
 * be extended
 */
public interface IAddress
{
	/*
	 * Return adds offset to address and returns new address object 
	 * which is the result  
	 */
	IAddress add(BigInteger offset);
	/*
	 * Returns maximal offset possible for address. The offset 
	 * should be Identicall for all addresses of given class. 
	 */
	BigInteger getMaxOffset();
	/*
	 * Returns distance between two addresses. Distance may be positive or negative
	 */
	BigInteger distance(IAddress other);
	/*
	 * Compares two addresses.
	 * 
	 * Returns:
	 *        -1 if this <  addr
	 *         0 if this == addr
	 *         1 if this >  addr
	 */
	int compareTo(IAddress addr);
	/*
	 * Returns true if addresses are equal
	 */
	boolean equals(IAddress addr);
    /*
     * Return true if address is zero, i.e. minimal possible
     */
	boolean isZero();
    /*
     * Return true if address is maximal, i.e. maximal possible
     */
	boolean isMax();
	
	/*
	 * Converts address to string as an unsigned number with given radix
	 */
	String toString(int radix);
	/*
	 * Identical to toString(10)
	 */
	String toString();
	/*
	 * Converts address to the hex representation with '0x' prefix and 
	 * with all leading zeros. The length of returned string should be 
	 * the same for all addresses of given class. I.e. 10 for 32-bit 
	 * addresses and 18 for 64-bit addresses
	 */
	String toHexAddressString();

	/*
	 * Returns amount of symbols in hex representation. Is identical to 
	 * toHexAddressString().length(). It is present for perfomance purpose.
	 */
	int getCharsNum();
	
}
