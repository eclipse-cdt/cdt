/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.movetype.MoveTypeRefactoringRunner;

/**
 * Launches an Extract Local Variable refactoring.
 * @since 6.5
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MoveTypeAction extends RefactoringAction {
    public MoveTypeAction() {
        super(Messages.MoveTypeAction_label);
    }

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new MoveTypeRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
	super.updateSelection(elem);
	setEnabled(false);
    }
}
