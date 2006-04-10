/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver.java;

/**
 * This class represents a UTF-8 string.
 */
public class UTF8Info extends AbstractCPInfo {
	
	protected int length;
	protected short[] bytes;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param length the length;
	 * @param bytes the array of bytes with the given length.
	 */
	public UTF8Info(short tag, int length, short[] bytes) {
		super(tag);
		setLength(length);
		setBytes(bytes);
	}
	
	/**
	 * Returns the bytes.
	 * @return the bytes.
	 */
	public short[] getBytes() {
		return bytes;
	}
	
	/**
	 * Sets the bytes.
	 * @param bytes the bytes.
	 */
	private void setBytes(short[] bytes) {
		this.bytes = bytes;
	}
	
	/**
	 * Returns the length.
	 * @return the length.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets the length.
	 * @param length the length.
	 */
	private void setLength(int length) {
		this.length = length;
	}
}