/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;

public interface ICExtension {
	public IProject getProject();
	public ICExtensionReference getExtensionReference();
}
