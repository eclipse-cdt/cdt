/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.ui.dialogs.ICOptionContainerExtension;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;

/**
 * @deprecated as of CDT 4.0. This abstract was used for New Project Wizards
 * for 3.X style projects.
 */
@Deprecated
public abstract class NewCProjectWizardOptionPage extends WizardPage implements ICOptionContainerExtension {

	private TabFolderOptionBlock fOptionBlock;

	public NewCProjectWizardOptionPage(String pageName) {
		this(pageName, null, null);
	}

	public NewCProjectWizardOptionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	protected abstract TabFolderOptionBlock createOptionBlock();

	@Override
	public void createControl(Composite parent) {
		fOptionBlock = createOptionBlock();
		setControl(fOptionBlock.createContents(parent));
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
		updateContainer();
	}

	@Override
	public void updateContainer() {
		fOptionBlock.update();
		setPageComplete(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	public void performApply(IProgressMonitor monitor) {
		fOptionBlock.performApply(monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferenceStore()
	 */
	@Override
	public abstract Preferences getPreferences();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	@Override
	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	@Override
	public IProject getProjectHandle() {
		return ((NewCProjectWizard)getWizard()).getProjectHandle();
	}
}
