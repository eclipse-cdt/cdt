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
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;

public abstract class InternalCExtension extends PlatformObject {

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
