/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class ResourcePropertyTester extends PropertyTester {
	private static final String PROP_PROJECT_BUILDER = "projectBuilder"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IResource) || args.length != 0)
			return false;
		if (property.equals(PROP_PROJECT_BUILDER)) {
			IProject project = ((IResource) receiver).getProject();
			if (project == null)
				return false;
			try {
				IProjectDescription description = project.getDescription();
				ICommand[] buildSpec = description.getBuildSpec();
				for (ICommand builder : buildSpec) {
					if (builder.getBuilderName().equals(expectedValue))
						return true;
				}
			} catch (CoreException e) {
				// Ignore to return false;
			}
		}
		return false;
	}
}
