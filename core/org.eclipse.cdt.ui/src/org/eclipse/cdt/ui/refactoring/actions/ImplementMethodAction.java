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
 * Institute for Software - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.ImplementMethodRefactoringRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;

/**
 * Launches the implement method source generator (refactoring).
 *
 * @author Lukas Felber
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ImplementMethodAction extends RefactoringAction {

	public ImplementMethodAction() {
		super(Messages.ImplementMethodAction_label);
	}

	/**
	 * @since 5.1
	 */
	public ImplementMethodAction(IEditorPart editor) {
		super(Messages.ImplementMethodAction_label);
		setEditor(editor);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		new ImplementMethodRefactoringRunner(elem, null, shellProvider, elem.getCProject()).run();
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		IResource res = wc.getResource();
		if (res instanceof IFile) {
			new ImplementMethodRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		if (elem instanceof IMethod || elem instanceof IMethodDeclaration == false
				|| elem instanceof ISourceReference == false
				|| ((ISourceReference) elem).getTranslationUnit().getResource() instanceof IFile == false) {
			setEnabled(false);
		}
	}
}
