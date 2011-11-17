/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

public abstract class AbstractCExtension extends PlatformObject implements ICExtension {
	private IProject fProject;
	@Deprecated
	private ICExtensionReference extensionRef;
	private ICConfigExtensionReference fCfgExtensionRef;

	/**
	 * Returns the project for which this extension is defined.
	 *
	 * @return the project
	 */
	@Override
	public final IProject getProject() {
		return fProject;
	}

	/**
	 * <strong>May return <code>null</code>!</strong>
	 * @deprecated Use {@link #getConfigExtensionReference()} instead.
	 */
	@Override
	@Deprecated
	public final ICExtensionReference getExtensionReference() {
		if (extensionRef == null) {
			// try to create one for the sake of backwards compatibility
			try {
				ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(getProject(), false);
				if (cdesc != null) {
					ICExtensionReference[] cextensions = cdesc.get(fCfgExtensionRef.getExtensionPoint(), false);
					for (ICExtensionReference ref : cextensions) {
						if (ref.getID().equals(fCfgExtensionRef.getID())) {
							extensionRef = ref;
							break;
						}
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return extensionRef;
	}

	/**
	 * Returns the extension reference this extension was created from.
	 * @since 5.2
	 */
	@Override
	public final ICConfigExtensionReference getConfigExtensionReference() {
		return fCfgExtensionRef;
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
	@Deprecated
	public void setExtensionReference(ICExtensionReference extReference) {
		extensionRef = extReference;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setExtensionReference(ICConfigExtensionReference extReference) {
		fCfgExtensionRef = extReference;
	}
}
