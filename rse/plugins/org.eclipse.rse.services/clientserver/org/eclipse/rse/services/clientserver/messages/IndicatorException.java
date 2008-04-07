/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;

/**
 * used to indicate that the indicator is not valid.
 * @deprecated This exception is no longer used and client code should not
 *    try catching it unless it wants to be compatible with earlier RSE versions. 
 */
public class IndicatorException extends RuntimeException 
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3884986031505435750L;

	public IndicatorException(String text) 
	{
		super(text);
	}
}	
