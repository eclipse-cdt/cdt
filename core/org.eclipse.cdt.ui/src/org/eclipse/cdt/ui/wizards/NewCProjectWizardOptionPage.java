/*
 * Created on 7-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public abstract class NewCProjectWizardOptionPage extends WizardPage implements ICOptionContainer {

	private TabFolderOptionBlock fOptionBlock;

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

	public abstract IProject getProject();
}
