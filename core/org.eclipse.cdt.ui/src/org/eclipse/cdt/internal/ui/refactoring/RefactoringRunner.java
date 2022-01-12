/*******************************************************************************
 * Copyright (c) 2011, 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Base class for all refactoring runners.
 */
public abstract class RefactoringRunner {
	protected final ISelection selection;
	protected final ICElement element;
	protected final ICProject project;
	protected final IShellProvider shellProvider;

	public RefactoringRunner(ICElement element, ISelection selection, IShellProvider shellProvider,
			ICProject cProject) {
		this.selection = selection;
		this.element = element;
		this.project = cProject;
		this.shellProvider = shellProvider;
	}

	public abstract void run();

	protected final void run(RefactoringWizard wizard, CRefactoring refactoring, int saveMode) {
		CRefactoringContext context = new CRefactoringContext(refactoring);
		try {
			RefactoringStarter starter = new RefactoringStarter();
			starter.activate(wizard, shellProvider.getShell(), refactoring.getName(), saveMode);
		} finally {
			context.dispose();
		}
	}
}
