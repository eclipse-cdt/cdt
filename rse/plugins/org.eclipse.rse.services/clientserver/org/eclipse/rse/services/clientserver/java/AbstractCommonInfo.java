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
 * This class represents information about a field or method.
 */
public abstract class AbstractCommonInfo {
	
	protected int accessFlags;
	protected int nameIndex;
	protected int descriptorIndex;
	protected int attributesCount;
	protected AbstractAttributeInfo[] attributes;

	/**
	 * Constructor.
	 * @param accessFlags the access flags.
	 * @param nameIndex the name index.
	 * @param descriptorIndex the descriptor index.
	 * @param attributesCount the number of attributes.
	 * @param attributes the attributes.
	 */
	public AbstractCommonInfo(int accessFlags, int nameIndex, int descriptorIndex, int attributesCount, AbstractAttributeInfo[] attributes) {
		super();
		setAccessFlags(accessFlags);
		setNameIndex(nameIndex);
		setDescriptorIndex(descriptorIndex);
		setAttributesCount(attributesCount);
		setAttributes(attributes);
	}
	
	/**
	 * Returns the access flags.
	 * @return the access flags.
	 */
	public int getAccessFlags() {
		return accessFlags;
	}
	
	/**
	 * Sets the access flags.
	 * @param accessFlags the access flags.
	 */
	public void setAccessFlags(int accessFlags) {
		this.accessFlags = accessFlags;
	}
	
	/**
	 * Returns the attributes.
	 * @return the array of attributes.
	 */
	public AbstractAttributeInfo[] getAttributes() {
		return attributes;
	}
	/**
	 * Sets the attributes.
	 * @param attributes the attributes.
	 */
	public void setAttributes(AbstractAttributeInfo[] attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Returns the number of attributes.
	 * @return the number of attributes.
	 */
	public int getAttributesCount() {
		return attributesCount;
	}
	
	/**
	 * Sets the number of attributes.
	 * @param attributesCount the number of attributes.
	 */
	public void setAttributesCount(int attributesCount) {
		this.attributesCount = attributesCount;
	}
	
	/**
	 * Returns the descriptor index.
	 * @return the descriptor index.
	 */
	public int getDescriptorIndex() {
		return descriptorIndex;
	}
	
	/**
	 * Sets the descriptor index.
	 * @param descriptorIndex the descriptor index.
	 */
	public void setDescriptorIndex(int descriptorIndex) {
		this.descriptorIndex = descriptorIndex;
	}
	
	/**
	 * Returns the name index.
	 * @return the name index.
	 */
	public int getNameIndex() {
		return nameIndex;
	}
	
	/**
	 * Sets the name index.
	 * @param nameIndex the name index.
	 */
	public void setNameIndex(int nameIndex) {
		this.nameIndex = nameIndex;
	}
}