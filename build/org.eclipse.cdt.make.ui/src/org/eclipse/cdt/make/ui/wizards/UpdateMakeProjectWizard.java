/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.actions.UpdateMakeProjectAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UpdateMakeProjectWizard extends Wizard {
	private static final String MAKE_UPDATE_WINDOW_TITLE = "MakeWizardUpdate.window_title"; //$NON-NLS-1$

	private UpdateMakeProjectWizardPage page1;
	private IProject[] selected;

	public UpdateMakeProjectWizard(IProject[] selected) {
		setDefaultPageImageDescriptor(null);
		setWindowTitle(MakeUIPlugin.getResourceString(MAKE_UPDATE_WINDOW_TITLE));
		setNeedsProgressMonitor(true);
		this.selected = selected;
	}

	@Override
	public boolean performFinish() {
		Object[] finalSelected = page1.getSelected();

		IProject[] projectArray = new IProject[finalSelected.length];
		System.arraycopy(finalSelected, 0, projectArray, 0, finalSelected.length);
		UpdateMakeProjectAction.run(true, getContainer(), projectArray);
		return true;
	}

	@Override
	public void addPages() {
		page1 = new UpdateMakeProjectWizardPage(selected);
		addPage(page1);
	}
}
