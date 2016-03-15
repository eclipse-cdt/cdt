/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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

/**
 * Represents an executable extension in the C model hierarchy.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICExtension {
	public IProject getProject();

	/**
	 * @deprecated Use {@link #getConfigExtensionReference()} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public ICExtensionReference getExtensionReference();

	/**
	 * @since 5.2
	 */
	public ICConfigExtensionReference getConfigExtensionReference();
}
