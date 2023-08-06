/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.ui.lsp;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * C Image Descriptor Provider
 * @since 8.1
 */
public interface ICFileImageDescriptor {

	/**
	 * @return ImageDescriptor for a C source file
	 */
	public ImageDescriptor getCImageDescriptor();

	/**
	 * @return ImageDescriptor for a C++ source file
	 */
	public ImageDescriptor getCXXImageDescriptor();

	/**
	 * @return ImageDescriptor for a C/C++ header file
	 */
	public ImageDescriptor getHeaderImageDescriptor();

	/**
	 * Checks whether the descriptor can be used for the given project.
	 * @param project
	 * @return true if the descriptor can be used for the given project.
	 */
	public boolean isEnabled(IProject project);

}
