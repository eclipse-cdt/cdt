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
package org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoring;

/**
 * Test harness for Extract Local Variable refactoring tests.
 * 
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoringTest extends RefactoringTest {
	protected String variableName;
	protected boolean fatalError;

	public ExtractLocalVariableRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileName);
		NameNVisibilityInformation info = new NameNVisibilityInformation();
		info.setName(variableName);
		CRefactoring refactoring = new ExtractLocalVariableRefactoring( refFile, selection, info, cproject);
		RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
		
		if (fatalError){
			assertConditionsFatalError(checkInitialConditions);
			return;
		}

		assertConditionsOk(checkInitialConditions);
		Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
		RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
		assertConditionsOk(finalConditions);
		createChange.perform(NULL_PROGRESS_MONITOR);		
		compareFiles(fileMap);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		variableName = refactoringProperties.getProperty("variablename", "temp"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
