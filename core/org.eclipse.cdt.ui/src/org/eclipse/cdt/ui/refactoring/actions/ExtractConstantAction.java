/*******************************************************************************
 * Copyright (c) 2005, 2012 Wind River Systems, Inc. 
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

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantRefactoringRunner;

/**
 * Launches an Extract Constant refactoring.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */          
public class ExtractConstantAction extends RefactoringAction {
    
    public ExtractConstantAction() {
        super(Messages.ExtractConstantAction_label);
    }
    
	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new ExtractConstantRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
    	super.updateSelection(elem);
    	setEnabled(false);
    }
}
