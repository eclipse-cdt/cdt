/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface ICDescriptor {
	public ICOwnerInfo getProjectOwner();
	public String getPlatform();
	public IProject getProject();
	
	public ICExtensionReference[] get(String extensionPoint);
	public ICExtensionReference[] get(String extensionPoint, boolean update) throws CoreException;
	public ICExtensionReference create(String extensionPoint, String id) throws CoreException;

	public void remove(ICExtensionReference extension) throws CoreException;
	public void remove(String extensionPoint) throws CoreException;
}
