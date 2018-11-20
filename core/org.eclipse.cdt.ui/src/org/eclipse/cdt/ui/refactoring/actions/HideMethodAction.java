/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoringRunner;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

/**
 * Launches a Hide Method refactoring
 * @author Guido Zgraggen IFS
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class HideMethodAction extends RefactoringAction {

	public HideMethodAction() {
		super(Messages.HideMethodAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		if (elem instanceof ISourceReference) {
			new HideMethodRefactoringRunner(elem, null, shellProvider, elem.getCProject()).run();
		}
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new HideMethodRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		if (!(elem instanceof IMethodDeclaration) || !(elem instanceof ISourceReference)
				|| ((ISourceReference) elem).getTranslationUnit().getResource() == null) {
			setEnabled(false);
		}
	}
}
