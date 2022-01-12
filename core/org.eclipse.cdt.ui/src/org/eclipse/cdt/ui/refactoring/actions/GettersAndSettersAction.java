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
 * 	   Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GenerateGettersAndSettersRefactoringRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;

/**
 * Launches a getter and setter source code generation.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GettersAndSettersAction extends RefactoringAction {

	public GettersAndSettersAction() {
		super(Messages.GettersAndSetters_label);
	}

	/**
	 * @since 5.1
	 */
	public GettersAndSettersAction(IEditorPart editor) {
		this();
		setEditor(editor);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		new GenerateGettersAndSettersRefactoringRunner(elem, null, shellProvider, elem.getCProject()).run();
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		if (wc.getResource() != null) {
			new GenerateGettersAndSettersRefactoringRunner(wc, s, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		if (!(elem instanceof IField) || !(elem instanceof ISourceReference)
				|| !(((ISourceReference) elem).getTranslationUnit().getResource() instanceof IFile)) {
			setEnabled(false);
		}
	}
}
