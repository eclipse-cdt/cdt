/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.implementmethod;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.ImplementMethodRefactoring;
 
/**
 * @author Mirko Stocker
 */
public class ImplementMethodRefactoringTest extends RefactoringTest {
	protected int finalWarnings;
	private int initialWarnings;

	public ImplementMethodRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileName);
		CRefactoring refactoring = new ImplementMethodRefactoring(refFile, selection, null, cproject);
		
		try {
			refactoring.lockIndex();
			RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);

			if(initialWarnings == 0) {
				assertConditionsOk(checkInitialConditions);
			} else {
				assertConditionsFatalError(checkInitialConditions, initialWarnings);
				return;
			}

			refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
			RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
			if (finalWarnings == 0) {
				Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
				assertConditionsOk(finalConditions);
				createChange.perform(NULL_PROGRESS_MONITOR);
			} else {
				assertConditionsWarning(finalConditions, finalWarnings);
			}
			compareFiles(fileMap);
		} finally {
			refactoring.unlockIndex();
		}
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		finalWarnings = new Integer(refactoringProperties.getProperty("finalWarnings", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
		initialWarnings = Integer.parseInt(refactoringProperties.getProperty("initialWarnings", "0"));  //$NON-NLS-1$//$NON-NLS-2$
	}
}
