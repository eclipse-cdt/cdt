/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * A property tester that determines if a file is an executable.
 */
public class CPropertyTester extends PropertyTester {
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isExecutable".equals(property)) { //$NON-NLS-1$
			return isExecutable(receiver);
		}
		return false;
	}

	/**
	 * Look for executable.
	 * @return true if the target resource has a <code>main</code> method,
	 * <code>false</code> otherwise.
	 */
	private boolean isExecutable(Object receiver) {
		ICElement celement = null;
		IFile file = (IFile) ((IAdaptable)receiver).getAdapter(IResource.class);
		if (file != null) {
			celement = CoreModel.getDefault().create(file);
		}
		return (celement != null && celement instanceof IBinary);
	}
	
}
