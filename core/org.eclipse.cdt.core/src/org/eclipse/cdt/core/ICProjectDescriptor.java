/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface ICProjectDescriptor {
	public static final String DESCRIPTION_FILE_NAME = ".cdtproject";
	
	public ICProjectOwnerInfo getProjectOwner();
	public String[] getPlatforms();
	public IProject getProject();
//	public IBuilderInfo getBuilderInfo();
//  public setBuilder(String id) or should this be add... ?
	public void saveInfo() throws CoreException;
}
