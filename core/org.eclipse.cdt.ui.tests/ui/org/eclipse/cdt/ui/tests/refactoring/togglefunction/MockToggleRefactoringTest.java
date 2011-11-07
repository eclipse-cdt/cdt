/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.togglefunction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleRefactoringContext;

public class MockToggleRefactoringTest extends ToggleRefactoring {

	public MockToggleRefactoringTest(IFile file, TextSelection selection, ICProject proj) {
		super(file, selection, proj);
	}

	public ToggleRefactoringContext getContext() {
		return context;
	}
}
