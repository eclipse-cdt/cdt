/*******************************************************************************
 * Copyright (c) 2005, 2010 Wind River Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google) 
 *******************************************************************************/

package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.actions.ActionUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;
import org.eclipse.cdt.internal.ui.refactoring.rename.RenameLinkedMode;

/**
 * Launches a rename refactoring.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */          
public class CRenameAction extends RefactoringAction {
    
    public CRenameAction() {
        super(Messages.CRenameAction_label);
        setSaveRequired(false);
    }
    
	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		if (!ActionUtil.isEditable((CEditor) fEditor, shellProvider.getShell(), elem))
			return;
		CRefactory.getInstance().rename(shellProvider.getShell(), elem);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy workingCopy, ITextSelection selection) {
		if (!ActionUtil.isEditable((CEditor) fEditor))
			return;
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		boolean lightweight= store.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
		if (lightweight) {
			new RenameLinkedMode((CEditor) fEditor).start();
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
