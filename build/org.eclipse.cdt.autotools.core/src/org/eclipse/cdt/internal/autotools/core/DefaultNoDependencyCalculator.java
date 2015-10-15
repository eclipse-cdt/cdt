/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.core;

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
@SuppressWarnings("deprecation")
public class DefaultNoDependencyCalculator implements IManagedDependencyGenerator {

	@Override
	public IResource[] findDependencies(IResource resource, IProject project) {
		// Never answers any dependencies
		return null;
	}

	@Override
	public int getCalculatorType() {
		return TYPE_NODEPS;
	}

	@Override
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		// Never answers this call with an actual value
		return null;
	}

}
