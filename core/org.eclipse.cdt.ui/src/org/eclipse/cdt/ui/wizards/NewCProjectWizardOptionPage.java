/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public abstract class NewCProjectWizardOptionPage extends WizardPage implements ICOptionContainer {

	private TabFolderOptionBlock fOptionBlock;
	private IPreferenceStore preferenceStore;

	public NewCProjectWizardOptionPage(String pageName) {
		this(pageName, null, null);
	}

	public NewCProjectWizardOptionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	protected abstract TabFolderOptionBlock createOptionBlock();

	public void createControl(Composite parent) {
		fOptionBlock = createOptionBlock();
		setControl(fOptionBlock.createContents(parent));
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
		updateContainer();
	}

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
	public abstract Preferences getPreferences();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public abstract IProject getProject();

}
