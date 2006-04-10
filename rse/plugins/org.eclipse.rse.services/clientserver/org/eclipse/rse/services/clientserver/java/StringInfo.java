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
 * This class represents a string information.
 */
public class StringInfo extends AbstractCPInfo {
	
	protected int stringIndex;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param stringIndex the string index.
	 */
	public StringInfo(short tag, int stringIndex) {
		super(tag);
		setStringIndex(stringIndex);
	}
	
	/**
	 * @return the stringIndex.
	 */
	public int getStringIndex() {
		return stringIndex;
	}

	/**
	 * Sets the stringIndex.
	 * @param stringIndex the stringIndex.
	 */
	private void setStringIndex(int stringIndex) {
		this.stringIndex = stringIndex;
	}
}