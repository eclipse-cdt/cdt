/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;

public abstract class AbstractCExtension extends PlatformObject implements ICExtension {
	private IProject fProject;
	private ICExtensionReference extensionRef;

	/**
	 * Returns the project for which this extrension is defined.
	 *	
	 * @return the project
	 */
	public final IProject getProject() {
		return fProject;
	}
	
	public final ICExtensionReference getExtensionReference() {
		return extensionRef;
	}
	

    // internal stuff
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setExtensionReference(ICExtensionReference extReference) {
		extensionRef = extReference;
	}
}
