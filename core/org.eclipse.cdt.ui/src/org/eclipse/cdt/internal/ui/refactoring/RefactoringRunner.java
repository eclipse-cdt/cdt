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
import org.eclipse.jface.window.IShellProvider;

/**
 * Base class for all refactoring runners.
 * 
 * @author Emanuel Graf
 *
 */
public abstract class RefactoringRunner {

	protected IFile file;
	protected ISelection selection;
	protected IShellProvider shellProvider;

	public RefactoringRunner(IFile file, ISelection selection, IShellProvider shellProvider) {
		this.file = file;
		this.selection = selection;
		this.shellProvider= shellProvider;
	}
	
	public abstract void run();

}
