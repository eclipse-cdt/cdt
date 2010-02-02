/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [208951] new priority field
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;

public interface ISystemFileTransferModeMapping {

	/**
	 * @since 3.2
	 */
	public static final int FILE_TYPE_BINARY = 0;
	/**
	 * @since 3.2
	 */
	public static final int FILE_TYPE_TEXT = 1;
	/**
	 * @since 3.2
	 */
	public static final int FILE_TYPE_XML = 2;

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
	
	/**
	 * Returns if the mapping is XML
	 * 
	 * @return true if XML
	 */
	public boolean isXML();
	
	/**
	 * Gets the priority - the smaller the number, the higher priority
	 * @return the priority
	 * @since 3.0
	 */
	public int getPriority();
}
