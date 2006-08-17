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
 * Represents C/C++ address in CDT. All implementors of this inteface should be 
 * immutable, i.e. all methods should not modify objects, they should return 
 * new object.
 * 
 * Please see Addr32 and Addr64 classes to see how this interface should 
 * be extended
 */
public interface IAddress extends Comparable
{
	/**
	 * Adds offset to address and returns new address object 
	 * which is the result  
	 * @param offset to add
	 * @return the new address
	 */
	IAddress add(BigInteger offset);
	
	/**
	 * Adds offset to address and returns new address object 
	 * which is the result  
	 * <br><br>Note: This method has an offset limit of Long.MAX and Long.MIN, which under some addressing schems 
	 * may impose an unnesseary limitation, see <code>IAddressa.add(BigInteger offset)</code> to handle larger offsets.
	 * @param offset to add
	 * @return the new address
	 */
	IAddress add(long offset);
	
	/**
	 * Returns maximal offset possible for address. The offset 
	 * should be Identicall for all addresses of given class. 
	 * @return the max offset for this address class
	 */
	BigInteger getMaxOffset();

	/**
	 * Returns distance to address. Distance may be positive or negative
	 * @param other address which distance is calculated to.
	 * @return distance to address
	 */
	BigInteger distanceTo(IAddress other);
	
	/**
	 * Returns the value of the address.
	 * @return
	 */
	BigInteger getValue();
		
	/**
	 * Returns whether this address equals the given object.
	 *
	 * @param obj the other object
	 * @return <code>true</code> if the addresses are equivalent,
	 *    and <code>false</code> if they are not
	 */
	boolean equals(Object addr);
 
	/**
     * Return true if address is zero, i.e. minimal possible
	 * @return true is address is zero
	 */
	boolean isZero();

	/**
     * Return true if address is maximal, i.e. maximal possible
	 * @return true if address is maximal
	 */
	boolean isMax();
	
	/**
	 * Converts address to string as an unsigned number with given radix
	 * @param radix to use for strng conversion
	 * @return a string representation of address
	 */
	String toString(int radix);

	/**
	 * Identical to toString(10)
	 * @return a string representation of address using a radix of 10
	 */
	String toString();

	/**
	 * Converts address to the hex representation with '0x' prefix and 
	 * with all leading zeros. The length of returned string should be 
	 * the same for all addresses of given class. I.e. 10 for 32-bit 
	 * addresses and 18 for 64-bit addresses
	 * @return
	 */
	String toHexAddressString();

	/**
	 * Returns amount of symbols in hex representation. Is identical to 
	 * toHexAddressString().length(). It is present for perfomance purpose.
	 * 
	 * @return the nmber os chararcter symbols to represent this address in hex.
	 */
	int getCharsNum();
	
	/**
	 * Returns the address size in bytes.
	 * 
	 * @return the number of bytes required to hold this address. 
	 */
	int getSize();
	
}
