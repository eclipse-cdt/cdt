/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.CoreException;

public interface ICOwner {
	public void configure(ICDescriptor cproject) throws CoreException;
	public void update(ICDescriptor cproject, String extensionID) throws CoreException;
}
