/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.CoreException;

public interface ICExtensionReference {
	
	/**
	 * Return the extension point of this reference.
	 * @return String
	 */
	public String getExtension(); 

	/**
	 * Return the extension ID of this reference.
	 * @return String
	 */
	public String getID();
	
	/**
	 * Sets a name/value data pair on this reference in the .cdtproject file
	 */
	public void setExtensionData(String key, String value);

	/**
	 * Gets a value of the key from the .cdtproject file set by setExtensionData()
	 */
	public String getExtensionData(String key);

	/**
	 * Creates the executable extension for the reference.
	 */	
	public ICExtension createExtension() throws CoreException;	
}
