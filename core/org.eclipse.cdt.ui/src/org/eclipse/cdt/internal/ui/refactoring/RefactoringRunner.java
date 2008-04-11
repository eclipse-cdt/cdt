/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Base class for all refactoring runners.
 * 
 * @author Emanuel Graf
 *
 */
public abstract class RefactoringRunner {

	protected IFile file;
	protected ISelection selection;
	protected Shell shell;

	public RefactoringRunner(IFile file, ISelection selection) {
		this.file = file;
		this.selection = selection;
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	public abstract void run();

}
