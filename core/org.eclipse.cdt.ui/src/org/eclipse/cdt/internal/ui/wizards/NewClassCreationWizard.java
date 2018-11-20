/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.NewClassCreationWizardPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewClassCreationWizard extends NewElementWizard {
	private NewClassCreationWizardPage fPage;
	private String className;

	public NewClassCreationWizard() {
		super();
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEWCLASS);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewClassWizardMessages.NewClassCreationWizard_title);
	}

	/*
	 * @see Wizard#createPages
	 */
	@Override
	public void addPages() {
		super.addPages();
		fPage = new NewClassCreationWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
		if (className != null)
			fPage.setClassName(className, true);
	}

	/**
	 * Sets the class name for creation in the wizard.
	 *
	 * @param className
	 *            Name of the class or null, null will indicate default behavior
	 *            which is extract class name from editor context. Setting an
	 *            empty string will force the empty fields.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	protected boolean canRunForked() {
		return !fPage.isNamespaceSelected();
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws CoreException {
		fPage.createClass(monitor); // use the full progress monitor
	}

	@Override
	public boolean performFinish() {
		boolean finished = super.performFinish();
		if (finished) {
			if (fPage.openClassInEditor()) {
				IFile source = fPage.getCreatedSourceFile();
				if (source != null) {
					selectAndReveal(source);
					openResource(source);
				}
				IFile header = fPage.getCreatedHeaderFile();
				if (header != null) {
					selectAndReveal(header);
					openResource(header);
				}
			}
		}
		return finished;
	}
}
