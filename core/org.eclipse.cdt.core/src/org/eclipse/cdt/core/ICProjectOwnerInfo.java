/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

public interface ICProjectOwnerInfo {
	public String getID();	
	public String getName();
	public String[] getPlatforms();
	public String[] getArchitectures(String platform);
}
