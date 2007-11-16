/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;


/**
 * An internal class. Clients must not instantiate or subclass it.
 */

public class SystemFileTransferModeMapping implements ISystemFileTransferModeMapping, Cloneable {
	
	public static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;
	private String name;
	private String extension;
	private boolean isBinary = true;
	private int priority = DEFAULT_PRIORITY;

	/**
	 * Constructor for SystemFileTransferModeMapping. The name is set to <code>*</code>.
	 * @param extension the extension. Can be <code>null</code>.
	 */
	public SystemFileTransferModeMapping(String extension) {
		this("*", extension); //$NON-NLS-1$
	}
		
	/**
	 * Constructor for SystemFileTransferModeMapping.
	 * @param name the name. If the name is <code>null</code> or if it is an empty string, it is set to <code>*</code>.
	 * @param extension the extension. Can be <code>null</code>.
	 */
	public SystemFileTransferModeMapping(String name, String extension) {
		
		if ((name == null) || (name.length() < 1)) {
			setName("*"); //$NON-NLS-1$
		}
		else {
			setName(name);
		}
		
		setExtension(extension);
	}



	/**
	 * @see ISystemFileTransferModeMapping#getExtension()
	 */
	public String getExtension() {
		return extension;
	}


	/**
	 * @see ISystemFileTransferModeMapping#getLabel()
	 */
	public String getLabel() {
		
		if (extension != null) {
			return (name + "." + extension);  //$NON-NLS-1$
		}
		else {
			return name;
		}
	}


	/**
	 * @see ISystemFileTransferModeMapping#getName()
	 */
	public String getName() {
		return name;
	}


	/**
	 * @see ISystemFileTransferModeMapping#isBinary()
	 */
	public boolean isBinary() {
		return isBinary;
	}


	/**
	 * @see ISystemFileTransferModeMapping#isText()
	 */
	public boolean isText() {
		return !isBinary();
	}
	
	
	/**
	 * Set whether transfer mode is binary
	 */
	public void setAsBinary() {
		isBinary = true;
	}
	
	
	/**
	 * Set whether transfer mode is text
	 */
	public void setAsText() {
		isBinary = false;
	}
	 
	
	/**
	 * Set the name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Set the extension
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	/**
	 * Set the priority - the smaller the number, the higher priority
	 * @param priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * Gets the priority - the smaller the number, the higher priority
	 * @return the priority
	 */
	public int getPriority()
	{
		return this.priority;
	}
	
	
	/**
	 * Clone this object
	 */
	public Object clone() {
		
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
