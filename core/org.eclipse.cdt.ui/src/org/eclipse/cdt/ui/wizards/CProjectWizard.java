/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.newui.UIMessages;

public class CProjectWizard extends CDTCommonProjectWizard {

	public CProjectWizard() {
		super(UIMessages.getString("NewModelProjectWizard.4"),  //$NON-NLS-1$
			  UIMessages.getString("NewModelProjectWizard.5")); //$NON-NLS-1$
	}

	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID };
	}

	protected IProject continueCreation(IProject prj) {
		try {
			CProjectNature.addCNature(prj, new NullProgressMonitor());
		} catch (CoreException e) {}
		return prj;
	}

}
