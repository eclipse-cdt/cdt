/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Verifies that a <code>{@link IResource}</code> can be processed by an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public interface ISupportedResourceVerifier {
	/**
	 * Indicates whether the external tool is capable of processing the given
	 * <code>{@link IResource}</code>.
	 * <p>
	 * The minimum requirements that the given {@code IResource} should satisfy are:
	 * <ul>
	 * <li>should be an <code>{@link IFile}</code></li>
	 * <li>should be displayed in the current active editor</li>
	 * <li>should not have any unsaved changes</li>
	 * </ul>
	 * </p>
	 * @param resource the given {@code IResource}.
	 * @return {@code true} if the external tool is capable of processing the given file,
	 *         {@code false} otherwise.
	 */
	boolean isSupported(IResource resource);
}
