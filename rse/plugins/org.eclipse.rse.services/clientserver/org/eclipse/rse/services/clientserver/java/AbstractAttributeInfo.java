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
 * This class represents a attribute.
 */
public class AbstractAttributeInfo {
	
	protected int attributeNameIndex;
	protected long attributeLength;

	/**
	 * Constructor.
	 * @param attributeNameIndex the attribute name index.
	 * @param attributeLength the attribute length.
	 */
	public AbstractAttributeInfo(int attributeNameIndex, long attributeLength) {
		super();
		setAttributeNameIndex(attributeNameIndex);
		setAttributeLength(attributeLength);
	}

	/**
	 * Returns the attribute length.
	 * @return the attribute length.
	 */
	public long getAttributeLength() {
		return attributeLength;
	}
	
	/**
	 * Sets the attribute length.
	 * @param attributeLength the attribute length.
	 */
	public void setAttributeLength(long attributeLength) {
		this.attributeLength = attributeLength;
	}
	
	/**
	 * Returns the attribute name index.
	 * @return the attribute name index.
	 */
	public int getAttributeNameIndex() {
		return attributeNameIndex;
	}
	
	/**
	 * Sets the attribute name index.
	 * @param attributeNameIndex the attribute name index.
	 */
	public void setAttributeNameIndex(int attributeNameIndex) {
		this.attributeNameIndex = attributeNameIndex;
	}
}