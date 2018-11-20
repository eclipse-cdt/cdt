/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;

/**
 * @since 3.1
 *
 * This interface is the base interface for IManagedDependencyCalculator,
 * IManagedDependencyCommands and IManagedDependencyPreBuild.  See these
 * interfaces and IManagedDependencyGenerator2 for more information on
 * writing a dependency calculator.
 *
 * The methods below simply return the arguments passed to the
 * IManagedDependencyGenerator2.getDependency*Info call that created the
 * IManagedDependencyInfo instance.
 *
 */
public interface IManagedDependencyInfo {
	public IPath getSource();

	public IBuildObject getBuildContext();

	public ITool getTool();

	public IPath getTopBuildDirectory();
}
