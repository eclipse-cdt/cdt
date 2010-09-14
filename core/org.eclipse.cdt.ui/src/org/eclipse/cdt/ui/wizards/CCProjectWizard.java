/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.newui.UIMessages;

/**
 * The wizard to create new MBS C++ Project.
 */
public class CCProjectWizard extends CDTCommonProjectWizard {

	public CCProjectWizard() {
		super(UIMessages.getString("NewModelProjectWizard.2"), UIMessages.getString("NewModelProjectWizard.3")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID };
	}
	
	@Override
	protected IProject continueCreation(IProject prj) {
		if (continueCreationMonitor == null) {
			continueCreationMonitor = new NullProgressMonitor();
		}
		
		try {
			continueCreationMonitor.beginTask(UIMessages.getString("CCProjectWizard.0"), 2); //$NON-NLS-1$
			CProjectNature.addCNature(prj, new SubProgressMonitor(continueCreationMonitor, 1));
			CCProjectNature.addCCNature(prj, new SubProgressMonitor(continueCreationMonitor, 1));
		} catch (CoreException e) {}
		finally {continueCreationMonitor.done();}
		return prj;
	}
	
	@Override
	public String[] getContentTypeIDs() {
		return new String[] { CCorePlugin.CONTENT_TYPE_CXXSOURCE, CCorePlugin.CONTENT_TYPE_CXXHEADER };
	}
	

}
