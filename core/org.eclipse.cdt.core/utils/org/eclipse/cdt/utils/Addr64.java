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
package org.eclipse.cdt.utils;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;

/*
 */
final public class Addr64 implements IAddress 
{
	public static final Addr64 ZERO=new Addr64("0");
	public static final Addr64 MAX=new Addr64("ffffffffffffffff",16);

	public static final BigInteger MAX_OFFSET = new BigInteger("ffffffffffffffff",16);
	
    public static final int BYTES_NUM = 8;
    public static final int DIGITS_NUM = BYTES_NUM * 2;
	public static final int CHARS_NUM = DIGITS_NUM + 2; 
	
	private BigInteger address;

	public Addr64(byte [] addrBytes)
	{
		if( addrBytes.length != 8)
			throw(new NumberFormatException("Invalid address array"));
		this.address = new BigInteger(1, addrBytes);
	}
	
	public Addr64(BigInteger rawaddress) 
	{
		this.address=rawaddress;		
	}	
	
	public Addr64(String addr) 
	{
		addr = addr.toLowerCase();
		if ( addr.startsWith( "0x" ) )
		{		
			this.address = new BigInteger(addr.substring(2), 16);
		}
		else
		{
			this.address = new BigInteger(addr, 10);
		}
	}
	
	public Addr64(String addr, int radix) 
	{
		this.address=new BigInteger(addr, radix);
	}

	final public IAddress add(BigInteger offset) 
	{
		return new Addr64(this.address.add(offset));
	}

	final public BigInteger getMaxOffset()
	{
		return MAX_OFFSET;
	}

	final public BigInteger distance(IAddress other)
	{
		return address.add(((Addr64)other).address.negate());
	}

	final public boolean isMax() 
	{
		return address.equals(MAX);
	}

	final public boolean isZero() 
	{
		return address.equals(ZERO);
	}

	final public int compareTo(IAddress addr) 
	{
		return this.address.compareTo(((Addr64)addr).address);
	}

	final public boolean equals(IAddress x) 
	{
		if (x == this)
			return true;
		if (!(x instanceof Addr64))
			return false;
		return this.address.equals(((Addr64)x).address);
	}

	final public String toString() 
	{
		return toString(10);		
	}
	
	final public String toString(int radix) 
	{
		return address.toString(radix);
	}
	
	final public String toHexAddressString( )
	{
		String addressString = address.toString(16);
		StringBuffer sb = new StringBuffer( CHARS_NUM  );
        int count = DIGITS_NUM - addressString.length();
		sb.append( "0x" );
		for ( int i = 0; i < count; ++i ) 
		{
			sb.append( '0' );
		}
		sb.append( addressString );
		return sb.toString();
	}
	
	final public int getCharsNum()
	{
		return CHARS_NUM;
	}
}

