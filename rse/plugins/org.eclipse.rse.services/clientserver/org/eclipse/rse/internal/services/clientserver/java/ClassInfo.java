/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.services.clientserver.java;

/**
 * This class represents class information.
 */
public class ClassInfo extends AbstractCPInfo {
	
	protected int nameIndex;

	/**
	 * Constructor.
	 * @param tag the tag.
	 * @param nameIndex the name index.
	 */
	public ClassInfo(short tag, int nameIndex) {
		super(tag);
		setNameIndex(nameIndex);
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
	private void setNameIndex(int nameIndex) {
		this.nameIndex = nameIndex;
	}
}
