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

package org.eclipse.rse.core.subsystems;
/**
 * Interface for resolving an object in a subsystem from a unique ID
 */
public interface IRemoteObjectResolver 
{


	/**
	 * For drag and drop, clipboard, and other object retrieval mechanisms in support of remote objects.
	 * <p>
	 * Return the remote object within the subsystem that corresponds to
	 * the specified unique ID.
	 * <p>
	 * This is the functional opposite of {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteName(Object)}.
	 */
	public Object getObjectWithAbsoluteName(String key) throws Exception;	
}