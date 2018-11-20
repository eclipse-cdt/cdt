/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.actions.RenameResourceAction;

public class CNavigatorRenameResourceAction extends RenameResourceAction {

	private IShellProvider shell;

	public CNavigatorRenameResourceAction(IShellProvider shell, Tree tree) {
		super(shell, tree);
		this.shell = shell;
	}

	@Override
	public void run() {
		List<? extends IResource> resources = getSelectedResources();

		if (resources.size() == 1) {
			IResource selectedResource = resources.get(0);
			if (selectedResource.exists()) {
				IProject project = selectedResource.getProject();
				if (hasCNature(project)) {
					CRefactory.getInstance().renameResource(shell.getShell(), selectedResource);
					return;
				}
			}
		}
		super.run();
	}

	private boolean hasCNature(IProject project) {
		boolean hasNature = false;
		try {
			hasNature = project.hasNature(CProjectNature.C_NATURE_ID)
					|| project.hasNature(CCProjectNature.CC_NATURE_ID);
		} catch (CoreException e1) {
			/*
			 * don't perform rename with CDT specific dialog if we can't test
			 * nature, it means that project either does not exist or is closed.
			 */
		}
		return hasNature;
	}

}
