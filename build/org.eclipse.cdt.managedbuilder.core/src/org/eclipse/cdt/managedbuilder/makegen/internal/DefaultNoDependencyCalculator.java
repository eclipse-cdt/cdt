/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.makegen.internal;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * This is the dependency calculator used by the makefile generation system when
 * nothing is defined for a tool.
 *
 * @since 2.0
 */
public class DefaultNoDependencyCalculator implements IManagedDependencyGenerator {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource, org.eclipse.core.resources.IProject)
	 */
	@Override
	public IResource[] findDependencies(IResource resource, IProject project) {
		// Never answers any dependencies
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	@Override
	public int getCalculatorType() {
		return TYPE_NODEPS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand(org.eclipse.core.resources.IResource)
	 */
	@Override
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		// Never answers this call with an actual value
		return null;
	}

}
