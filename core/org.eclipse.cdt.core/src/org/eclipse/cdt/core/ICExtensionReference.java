/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

public interface ICExtensionReference {
	public String getExtension();
	public String getID();
	public void setExtensionData(String key, String value);
	public String getExtensionData(String key);	
}
