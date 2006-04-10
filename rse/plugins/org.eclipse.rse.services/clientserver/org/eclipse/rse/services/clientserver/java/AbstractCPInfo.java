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
 * This class represents various string constants, class and interface names, field names, and other constants
 * that are referred to within the BasicClassFileParser structure.
 * 
 * @see BasicClassFileParser
 */
public abstract class AbstractCPInfo {
	
	protected short tag;

	/**
	 * Constructor.
	 * @param tag the tag.
	 */
	public AbstractCPInfo(short tag) {
		super();
		setTag(tag);
	}
	
	/**
	 * Returns the tag.
	 * @return the tag.
	 */
	public int getTag() {
		return tag;
	}
	
	/**
	 * Sets the tag.
	 * @param tag the tag.
	 */
	private void setTag(short tag) {
		this.tag = tag;
	}
}