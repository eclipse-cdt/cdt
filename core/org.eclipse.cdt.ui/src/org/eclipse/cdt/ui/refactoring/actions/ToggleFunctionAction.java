/*******************************************************************************
 * Copyright (c) 2011, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleRefactoringRunner;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @since 5.3
 * @author Emanuel Graf IFS
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ToggleFunctionAction extends RefactoringAction {

	public ToggleFunctionAction() {
		super(Messages.ToggleFunctionAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc == null || wc.getResource() == null)
			return;
		IWorkbenchPage activePage = CUIPlugin.getActivePage();
		if (activePage == null)
			return;
		IEditorPart editor = activePage.getActiveEditor();
		if (editor == null || editor.getEditorInput() == null)
			return;
		new ToggleRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		setEnabled(false);
	}
}
