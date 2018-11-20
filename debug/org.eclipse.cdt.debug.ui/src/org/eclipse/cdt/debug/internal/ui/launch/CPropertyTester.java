/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Ken Ryall (Nokia) - Modified to launch on a project context.
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * A property tester that determines if a file is an executable or a C/C++ project.
 */
public class CPropertyTester extends PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isExecutable".equals(property)) //$NON-NLS-1$
			return isExecutable(receiver);
		if ("isCProject".equals(property)) //$NON-NLS-1$
			return isCProject(receiver);
		return false;
	}

	private boolean isExecutable(Object receiver) {
		ICElement celement = null;
		if (receiver instanceof IAdaptable) {
			IResource res = ((IAdaptable) receiver).getAdapter(IResource.class);
			if (res != null) {
				celement = CoreModel.getDefault().create(res);
			}
		}
		return celement != null && celement instanceof IBinary;
	}

	private boolean isCProject(Object receiver) {
		if (receiver instanceof IProject)
			return CoreModel.hasCNature((IProject) receiver);
		if (receiver instanceof ICProject)
			return true;
		return false;
	}
}
