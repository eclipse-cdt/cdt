/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.hidemethod;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoring;

/**
 * @author Guido Zgraggen IFS
 */
public class HideMethodRefactoringTest extends RefactoringTest {
	
	public HideMethodRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileWithSelection);
		Refactoring refactoring = new HideMethodRefactoring(refFile,selection, null, cproject);
		executeRefactoring(refactoring);
		compareFiles(fileMap);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		fatalError = Boolean.valueOf(refactoringProperties.getProperty("fatalerror", "false")).booleanValue();  //$NON-NLS-1$//$NON-NLS-2$
		finalWarnings = new Integer(refactoringProperties.getProperty("warnings", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
		initialErrors = new Integer(refactoringProperties.getProperty("errors", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}
}
