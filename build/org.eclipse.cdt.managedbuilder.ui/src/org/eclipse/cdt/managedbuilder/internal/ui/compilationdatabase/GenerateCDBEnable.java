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

package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import org.eclipse.core.resources.IProject;

public interface GenerateCDBEnable {

	/**
	 * Checks whether the generation of the compilation database file should be enabled for the given project.
	 * The enable can be linked with certain project properties (e.g. project natures).
	 * @param project
	 * @return true when the generation of compilation database file should be enabled for the given project
	 */
	public boolean isEnabledFor(IProject project);
}
