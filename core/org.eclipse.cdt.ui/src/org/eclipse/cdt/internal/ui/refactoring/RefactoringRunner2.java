/*******************************************************************************
 * Copyright (c) 2011, 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

/**
 * Base class for all refactoring runners. This class is intended as a replacement
 * for RefactoringRunner.
 */
public abstract class RefactoringRunner2 {
	protected final ISelection selection;
	protected final ICElement element;
	protected final ICProject project;
	private final IShellProvider shellProvider;

	public RefactoringRunner2(ICElement element, ISelection selection, IShellProvider shellProvider,
			ICProject cProject) {
		this.selection = selection;
		this.element= element;
		this.project = cProject;
		this.shellProvider= shellProvider;
	}

	public abstract void run();

	protected final void run(RefactoringWizard wizard, CRefactoring2 refactoring, int saveMode) {
		CRefactoringContext context = new CRefactoringContext(refactoring);
		try {
			RefactoringStarter starter = new RefactoringStarter();
			starter.activate(wizard, shellProvider.getShell(), refactoring.getName(), saveMode);
		} finally {
			context.dispose();
		}
	}
}
