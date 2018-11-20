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
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoringRunner;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

/**
 * Launches an Extract Local Variable refactoring.
 * @since 5.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExtractLocalVariableAction extends RefactoringAction {

	public ExtractLocalVariableAction() {
		super(Messages.ExtractLocalVariableAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new ExtractLocalVariableRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		setEnabled(false);
	}
}
