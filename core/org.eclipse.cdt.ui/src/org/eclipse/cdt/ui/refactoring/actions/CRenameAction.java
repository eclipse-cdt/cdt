/*******************************************************************************
 * Copyright (c) 2005, 2009 Wind River Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;

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
        CRefactory.getInstance().rename(shellProvider.getShell(), elem);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		CRefactory.getInstance().rename(shellProvider.getShell(), wc, s);
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
