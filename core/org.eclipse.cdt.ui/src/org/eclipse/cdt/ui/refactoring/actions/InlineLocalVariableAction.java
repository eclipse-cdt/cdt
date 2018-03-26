/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable.InlineLocalVariableRefactoringRunner;

/**
 * Launches an Extract Local Variable refactoring.
 * @since 6.5
 * @noextend This class is not intended to be subclassed by clients.
 */
public class InlineLocalVariableAction extends RefactoringAction {

	public InlineLocalVariableAction() {
		super(Messages.InlineLocalVariableAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new InlineLocalVariableRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		setEnabled(false);
	}
}
