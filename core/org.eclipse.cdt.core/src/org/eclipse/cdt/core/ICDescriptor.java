/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;

public interface ICDescriptor {
	public ICOwnerInfo getProjectOwner();
	public String getPlatform();
	public IProject getProject();
	public ICExtensionReference[] get(String name);
	public ICExtensionReference create(String name, String id);
	public void remove(ICExtensionReference extension);
}
