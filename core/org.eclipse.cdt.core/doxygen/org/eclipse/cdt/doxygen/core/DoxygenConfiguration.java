/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.doxygen.core;

import org.eclipse.cdt.doxygen.DoxygenOptions;
import org.eclipse.core.resources.IProject;

/**
 * Provides access to the doxygen options according to the required preference scope
 *
 */
public interface DoxygenConfiguration {

	/**
	 * Returns the doxygen options for the workspace scope, must not return <code>null</code>
	 *
	 * @return doxygen options for the workspace scope
	 */
	DoxygenOptions workspaceOptions();

	/**
	 * Returns the doxygen options for the given project scope, must not return <code>null</code>
	 *
	 * @param project scope for doxygen options, must not be <code>null</code>
	 * @return doxygen options for the given project scope
	 */
	DoxygenOptions projectOptions(IProject project);

}
