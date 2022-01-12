/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.build.ICBuildConfiguration;

/**
 * @since 7.0
 *
 */
public interface ICommandLauncherFactory3 {

	/**
	 * Check if any copied header files have changed.  This applies when using
	 * Flatpak which needs to copy the host's header files to the workspace.
	 *
	 * @param cfg - C Build configuration to check for
	 * @return true if headers have been removed/changed since last copy, false otherwise
	 */
	public boolean checkIfIncludesChanged(ICBuildConfiguration cfg);

}
