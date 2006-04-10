/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.shells.subsystems;

import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;

/**
 * The RemoteOuputImpl class is an implementation of IRemoteOutput.
 * It is used for storing information about a particular line of output
 */
public class RemoteError extends RemoteOutput implements IRemoteError
{

	/**
	 * Constructor
	 * @param parent container of the output
	 */
	public RemoteError(Object parent, String type)
	{
		super(parent, type);
	}



}