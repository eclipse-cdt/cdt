/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.hidemethod;

import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoring;

/**
 * @author Guido Zgraggen IFS
 */
public class HideMethodRefactoringTest extends RefactoringTest {
	private int warnings;
	private int errors;
	private int fatalerrors;
	
	public HideMethodRefactoringTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileWithSelection);
		CRefactoring refactoring = new HideMethodRefactoring(refFile,selection, null, cproject);
		RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
		if (errors > 0) {
			assertConditionsError(checkInitialConditions, errors);
		} else if (fatalerrors > 0) {
			assertConditionsError(checkInitialConditions, errors);
			return;
		} else {
			assertConditionsOk(checkInitialConditions);
		}

		Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
		RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
		if (warnings > 0) {
			assertConditionsWarning(finalConditions, warnings);
		} else {
			assertConditionsOk(finalConditions);
		}
		createChange.perform(NULL_PROGRESS_MONITOR);
		compareFiles(fileMap);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		warnings = new Integer(refactoringProperties.getProperty("warnings", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
		errors = new Integer(refactoringProperties.getProperty("errors", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
		fatalerrors = new Integer(refactoringProperties.getProperty("fatalerrors", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}
}
