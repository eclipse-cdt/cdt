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
 * This class represents 8 byte numeric information.
 */
public abstract class Abstract8ByteNumericInfo extends AbstractCPInfo {
	
	protected long highBytes;
	protected long lowBytes;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param highBytes high bytes.
	 * @param lowBytes low bytes.
	 */
	public Abstract8ByteNumericInfo(short tag, long highBytes, long lowBytes) {
		super(tag);
		setHighBytes(highBytes);
		setLowBytes(lowBytes);
	}
	
	/**
	 * Returns the high bytes.
	 * @return the high bytes.
	 */
	public long getHighBytes() {
		return highBytes;
	}
	
	/**
	 * Sets the high bytes.
	 * @param highBytes the high bytes.
	 */
	private void setHighBytes(long highBytes) {
		this.highBytes = highBytes;
	}
	
	/**
	 * Returns the low bytes.
	 * @return the low bytes.
	 */
	public long getLowBytes() {
		return lowBytes;
	}
	
	/**
	 * Sets the low bytes.
	 * @param lowBytes the low bytes.
	 */
	private void setLowBytes(long lowBytes) {
		this.lowBytes = lowBytes;
	}
}