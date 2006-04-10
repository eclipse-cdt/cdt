/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.core.model;

public interface ISystemFileTransferModeMapping {




	/**
	 * Get the extension
	 * 
	 * @return the extension for the mapping
	 */
	public String getExtension();
	
	
	/**
	 * Get the label
	 * 
	 * @return the label for the mapping
	 */
	public String getLabel();
	
	
	/**
	 * Get the name
	 * 
	 * @return the name for the mapping
	 */
	public String getName();
	
	
	/**
	 * Returns if the mapping is binary
	 * 
	 * @return true if binary, false if text
	 */
	public boolean isBinary();
	
	
	/**
	 * Returns if the mapping is text
	 * 
	 * @return true if text, false if binary
	 */
	public boolean isText();
}