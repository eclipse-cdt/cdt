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
final public class Addr32 implements IAddress
{
	public static final Addr32 ZERO=new Addr32(0);
	public static final Addr32 MAX=new Addr32(0xffffffffL);

	public static final BigInteger MAX_OFFSET = BigInteger.valueOf(0xffffffffL);
	
	public static final int BYTES_NUM = 4;
	public static final int DIGITS_NUM = BYTES_NUM * 2;
	public static final int CHARS_NUM = DIGITS_NUM + 2; 
		
	private long address;

	/*
	 * addrBytes should be 4 bytes length
	 */
	public Addr32(byte [] addrBytes)
	{
		/*We should mask out sign bits to have correct value*/
		this.address = ( ( ((long)addrBytes[0]) << 24 ) & 0xFF000000L) + 
                       ( ( ((long)addrBytes[1]) << 16 ) & 0x00FF0000L) +
                       ( ( ((long)addrBytes[2]) << 8  ) & 0x0000FF00L) + 
                       (   ((long)addrBytes[3])         & 0x000000FFL);
	}

	public Addr32(long rawaddress) 
	{
		this.address=rawaddress;
	}

	public Addr32(String addr) 
	{
		addr = addr.toLowerCase();
		if ( addr.startsWith( "0x" ) )
		{		
			this.address = Long.parseLong(addr.substring(2), 16);
		}
		else
		{
			this.address = Long.parseLong(addr, 10);
		}
	}

	public Addr32(String addr, int radix) 
	{
		this.address=Long.parseLong(addr, radix);
	}

	final public IAddress add(BigInteger offset) 
	{
		return new Addr32(this.address +  offset.longValue());
	}

	final public BigInteger getMaxOffset()
	{
		return MAX_OFFSET;
	}

	final public BigInteger distance(IAddress other)
	{
		return BigInteger.valueOf(address - ((Addr32)other).address);
	}
	
	final public int compareTo(IAddress addr) 
	{
		if (address > ((Addr32)addr).address)
		{ 
			return 1;
		}	
		if (address < ((Addr32)addr).address)
		{ 
			return -1;
		}	
		return 0;
	}

	final public boolean isMax() 
	{
		return address == MAX.address;
	}

	final public boolean isZero() 
	{
		return address == ZERO.address;
	}

	final public String toString() 
	{
		return toString(10);		
	}

	final public String toString(int radix) 
	{
		return Long.toString(address, radix);
	}

	final public boolean equals(IAddress x) 
	{
		if (x == this)
			return true;
		if (!(x instanceof Addr32))
			return false;
		return this.address == ((Addr32)x).address;
	}

	final public String toHexAddressString( )
	{
		String addressString = Long.toString(address,16);
		StringBuffer sb = new StringBuffer( CHARS_NUM  );
        int count = DIGITS_NUM - addressString.length();
		sb.append( "0x" );
		for ( int i = 0; i < count ; ++i ) 
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
