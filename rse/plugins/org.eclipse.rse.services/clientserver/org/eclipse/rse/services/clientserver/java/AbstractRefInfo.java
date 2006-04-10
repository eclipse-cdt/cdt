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
 * This class represents reference information.
 */
public abstract class AbstractRefInfo extends AbstractCPInfo {
	
	protected int classIndex;
	protected int nameAndTypeIndex;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param classIndex the class index.
	 * @param nameAndTypeIndex  
	 */
	public AbstractRefInfo(short tag, int classIndex, int nameAndTypeIndex) {
		super(tag);
		setClassIndex(classIndex);
		setNameAndTypeIndex(nameAndTypeIndex);
	}

	/**
	 * Returns the class index.
	 * @return the class index.
	 */
	public int getClassIndex() {
		return classIndex;
	}
	
	/**
	 * Sets the class index.
	 * @param classIndex the class index.
	 */
	private void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}
	
	/**
	 * Returns the name and type index.
	 * @return the name and type index.
	 */
	public int getNameAndTypeIndex() {
		return nameAndTypeIndex;
	}
	
	/**
	 * Sets the name and type index.
	 * @param nameAndTypeIndex the name and type index.
	 */
	private void setNameAndTypeIndex(int nameAndTypeIndex) {
		this.nameAndTypeIndex = nameAndTypeIndex;
	}
}