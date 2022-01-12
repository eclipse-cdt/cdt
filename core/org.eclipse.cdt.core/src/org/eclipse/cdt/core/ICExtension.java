/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
