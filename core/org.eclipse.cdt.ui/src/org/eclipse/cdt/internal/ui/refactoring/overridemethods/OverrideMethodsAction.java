/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OverrideMethodsAction extends RefactoringAction {

	public OverrideMethodsAction() {
		super(Messages.OverrideMethods_label);
	}

	public OverrideMethodsAction(IEditorPart editor) {
		this();
		setEditor(editor);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new OverrideMethodsRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		new OverrideMethodsRefactoringRunner(elem, null, shellProvider, elem.getCProject()).run();
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		if (elem != null && elem.getElementType() != ICElement.C_CLASS) {
			setEnabled(false);
		}
	}
}
