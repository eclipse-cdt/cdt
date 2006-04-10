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
 * This class represents 4 byte numeric information.
 */
public abstract class Abstract4ByteNumericInfo extends AbstractCPInfo {
	
	protected long bytes;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param bytes the bytes.
	 */
	public Abstract4ByteNumericInfo(short tag, long bytes) {
		super(tag);
		setBytes(bytes);
	}
	
	/**
	 * Returns the bytes.
	 * @return the bytes.
	 */
	public long getBytes() {
		return bytes;
	}
	
	/**
	 * Sets the bytes.
	 * @param bytes the bytes.
	 */
	private void setBytes(long bytes) {
		this.bytes = bytes;
	}
}