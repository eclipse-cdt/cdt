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
 * This class represents name and type information.
 */
public class NameAndTypeInfo extends ClassInfo {
	
	protected int descriptorIndex;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param nameIndex the name index.
	 * @param descriptorIndex the descriptor index.
	 */
	public NameAndTypeInfo(short tag, int nameIndex, int descriptorIndex) {
		super(tag, nameIndex);
		setDescriptorIndex(descriptorIndex);
	}
	
	/**
	 * Returns the descriptor index.
	 * @return the descriptorIndex.
	 */
	public int getDescriptorIndex() {
		return descriptorIndex;
	}

	/**
	 * Sets the descriptor index.
	 * @param descriptorIndex the descriptor index.
	 */
	private void setDescriptorIndex(int descriptorIndex) {
		this.descriptorIndex = descriptorIndex;
	}
}