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


/**
 * An internal class. Clients must not instantiate or subclass it.
 */

public class SystemFileTransferModeMapping
	implements ISystemFileTransferModeMapping, Cloneable {


		
	
	private String name;
	private String extension;
	private boolean isBinary = true;


	/**
	 * Constructor for SystemFileTransferModeMapping
	 */
	public SystemFileTransferModeMapping(String extension) {
		this("*", extension);
	}
		
	
	/**
	 * Constructor for SystemFileTransferModeMapping
	 */
	public SystemFileTransferModeMapping(String name, String extension) {
		
		if ((name == null) || (name.length() < 1)) {
			setName("*");
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
		return (name + "." + extension);
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
	 * Clone this object
	 */
	public Object clone() {
		
		try {
			return ((SystemFileTransferModeMapping)(super.clone()));
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
}