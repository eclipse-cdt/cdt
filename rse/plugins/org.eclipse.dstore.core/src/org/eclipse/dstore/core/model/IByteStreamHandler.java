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

package org.eclipse.dstore.core.model;

/**
 * <p>
 * The ByteStreamHandler interface is used to abstract file read and write operations
 * across the network. 
 * 
 */
public interface IByteStreamHandler
{

	/**
	 * Returns the unique ID for this bytestream handler
	 * @return the unique id
	 */
	public String getId();
	
	/**
	 * Save a file in the specified location.  This method is called by the
	 * DataStore when the communication layer receives a file transfer    
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the bytes to insert in the file
	 * @param size the number of bytes to insert
	 * @param binary indicates whether to save the bytes as binary or text
	 */
	public void receiveBytes(String remotePath, byte[] buffer, int size, boolean binary);

	/**
	 * Append a bytes to a file at a specified location. This method is called by the
	 * DataStore when the communication layer receives a file transfer append.      
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the bytes to append in the file
	 * @param size the number of bytes to append in the file
	 * @param binary indicates whether to save the bytes as binary or text
	 */
	public void receiveAppendedBytes(String remotePath, byte[] buffer, int size, boolean binary);
	

	

}