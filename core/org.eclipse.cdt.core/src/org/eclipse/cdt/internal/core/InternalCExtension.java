/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;

public abstract class InternalCExtension extends PlatformObject {

	private IProject fProject;
	private ICExtensionReference extensionRef;
		
	void setProject(IProject project) {
		fProject = project;
	}

	void setExtenionReference(ICExtensionReference extReference) {
		extensionRef = extReference;
	}
		
	protected IProject getProject() {
		return fProject;
	}

	protected ICExtensionReference getExtensionReference() {
		return extensionRef;
	}
}
