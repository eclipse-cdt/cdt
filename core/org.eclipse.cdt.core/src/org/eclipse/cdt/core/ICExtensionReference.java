/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
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
