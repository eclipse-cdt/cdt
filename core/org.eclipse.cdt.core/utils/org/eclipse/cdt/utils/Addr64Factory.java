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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;


/*
 */
final public class Addr64Factory implements IAddressFactory{

    final public  IAddress getZero() 
	{
		return Addr64.ZERO; 
	}

    final public IAddress getMax() 
	{
		return Addr64.MAX; 
	}
	
    final public IAddress createAddress(String addr) 
	{
		IAddress address=new Addr64(addr);
		return address; 
	}
	
    final public IAddress createAddress(String addr, int radix) 
	{
		IAddress address=new Addr64(addr, radix);
		return address; 
	}
}
