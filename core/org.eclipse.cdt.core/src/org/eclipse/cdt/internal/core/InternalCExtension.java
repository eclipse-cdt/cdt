/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.resources.IProject;

public abstract class InternalCExtension {

	private IProject project;
	private ICExtensionReference extensionRef;
		
	void setProject(IProject project) {
		this.project = project;
	}

	void setExtenionReference(ICExtensionReference extReference) {
		extensionRef = extReference;
	}
		
	protected IProject getProject() {
		return project;
	}

	protected ICExtensionReference getExtensionReference() {
		return extensionRef;
	}
}
