/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.dstore.core.java;

/**
 * <p>
 * The IClassByteStreamHandler interface is used to abstract file read and write operations
 * across the network. 
 * 
 */
public interface IClassByteStreamHandler
{

	/**
	 * Returns the unique ID for this bytestream handler
	 * @return the unique id
	 */
	public String getIdentifier();
	
	/**
	 * Receive a class and load it.  This method is called by the
	 * DataStore when the communication layer receives a class file transfer
	 * This method kicks off a new thread so that the receiver thread can be free
	 * to receive other data.
	 *
	 * @param className the name of the class to receive
	 * @param buffer the bytes that comprise the class
	 * @param size the number of bytes in the class
	 */
	public void receiveBytes(String className, byte[] buffer, int size);
	
	/**
	 * Save a class instance in the specified location. Invokes the operation in a new thread.  This method is called by the
	 * DataStore when the communication layer receives a class file transfer    
	 *
	 * @param buffer the bytes to insert in the class instance
	 * @param size the number of bytes to insert
	 */
	public void receiveInstanceBytes(byte[] buffer, int size);
}