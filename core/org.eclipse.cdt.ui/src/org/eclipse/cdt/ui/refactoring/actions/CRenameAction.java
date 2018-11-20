/*******************************************************************************
 * Copyright (c) 2005, 2012 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.actions.ActionUtil;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;
import org.eclipse.cdt.internal.ui.refactoring.rename.RenameLinkedMode;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

/**
 * Launches a rename refactoring.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CRenameAction extends RefactoringAction {

	public CRenameAction() {
		super(Messages.CRenameAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		if (!ActionUtil.isEditable(fEditor, shellProvider.getShell(), elem))
			return;
		CRefactory.getInstance().rename(shellProvider.getShell(), elem);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy workingCopy, ITextSelection selection) {
		if (!ActionUtil.isEditable(fEditor))
			return;
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		boolean lightweight = store.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
		if (lightweight) {
			new RenameLinkedMode(fEditor).start();
		} else {
			CRefactory.getInstance().rename(shellProvider.getShell(), workingCopy, selection);
		}
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		if (elem == null || elem instanceof IInclude || elem instanceof ITranslationUnit) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}
}
