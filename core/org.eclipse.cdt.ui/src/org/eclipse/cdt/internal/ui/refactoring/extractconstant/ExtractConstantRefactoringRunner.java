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
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

/**
 * @author Emanuel Graf
 */
public class ExtractConstantRefactoringRunner extends RefactoringRunner {

	public ExtractConstantRefactoringRunner(ICElement element, ISelection selection, IShellProvider shellProvider,
			ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run() {
		ExtractConstantRefactoring refactoring = new ExtractConstantRefactoring(element, selection, project);
		ExtractConstantWizard wizard = new ExtractConstantWizard(refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
	}
}
