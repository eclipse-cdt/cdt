/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
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
 * A property tester that determines if a file is an executable.
 */
public class CPropertyTester extends PropertyTester {
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isExecutable".equals(property)) //$NON-NLS-1$
			return isExecutable(receiver);
		else if ("isCProject".equals(property)) //$NON-NLS-1$
			return isCProject(receiver);
		else
			return false;
	}

	private boolean isExecutable(Object receiver) {
		ICElement celement = null;
		if (receiver instanceof IAdaptable) {
			IResource res = (IResource) ((IAdaptable)receiver).getAdapter(IResource.class);
			if (res != null) {
				celement = CoreModel.getDefault().create(res);
			}
		}
		return (celement != null && celement instanceof IBinary);
	}
	
	private boolean isCProject(Object receiver) {
		if (receiver instanceof IProject)
			return CoreModel.hasCNature((IProject)receiver);
		else if (receiver instanceof ICProject)
			return true;
		else
			return false;
	}
	
}
