/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Ken Ryall (Nokia) - Modified to launch on a project context.
 *     Marc Khouzam (Ericsson) - Added support of 'isCore' property (Bug 410112)
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
		if ("isCore".equals(property)) //$NON-NLS-1$
			return isCore(receiver);
		return false;
	}

	private boolean isExecutable(Object receiver) {
		ICElement celement = null;
		if (receiver instanceof IAdaptable) {
			IResource res = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
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

	private boolean isCore(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IResource res = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if (res != null) {
				ICElement celement = CoreModel.getDefault().create(res);
				if (celement instanceof IBinary) {
					if (((IBinary)celement).isCore()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
