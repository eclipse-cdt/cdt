/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.model;

import java.util.List;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * This interface is refers to a collection of any type of resources.  The resources
 * may be remote RSE resources, or local workspace resources. 
 * @noimplement This interface is not intended to be implemented by clients.
 * Extend {@link AbstractSystemResourceSet} instead.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemResourceSet {
	
	/**
	 * Returns the number of resources in the set
	 * @return the number of resources in the set
	 */
	public int size();

	/**
	 * Returns the resource in the set with the specified absoluteName
	 * @param absoluteName the path of the resource to return
	 * @return the resource
	 */
	public Object get(String absoluteName);

	/**
	 * Returns the path of a specified resource.  This is the same path
	 * that is used be the get(absoluteName) method to retrieve the object
	 * @param obj the resource to return the path for
	 * @return the path of the resource
	 */
	public String pathFor(Object obj);

	/**
	 * Returns the resource at the specified index
	 * @param index the index of the resource to return
	 * @return the resource
	 */
	public Object get(int index);

	/**
	 * Returns the set of resources as a List
	 * @return the set of resources as a List
	 */
	public List getResourceSet();

	/**
	 * Returns a message if a problem occurs when using this resource set
	 * @return the message
	 */
	public SystemMessage getMessage();

	/**
	 * Indicates whether a message was set corresponding to this resource set
	 * during an operation against it.
	 * @return true if there is a message
	 */
	public boolean hasMessage();

	/**
	 * Indicates whether the set of resources has a size in bytes greater than zero
	 * @return true if there are more than 0 bytes in this set
	 */
	public boolean hasByteSize();

	/**
	 * Returns the total number of bytes in this collection of resources
	 * @return the number of bytes
	 */
	public long byteSize();

	/**
	 * Sets the total number of bytes in this collection of resources
	 * @param byteSize the number of bytes
	 */
	public void setByteSize(long byteSize);
}
