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

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

public class ToggleRefactoringTest extends RefactoringTest {
	private boolean fatalError;
	private boolean newFileCreation;
	private String[] newfiles;

	public ToggleRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		fatalError = Boolean.valueOf(refactoringProperties.getProperty("fatalerror", "false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
		newFileCreation = Boolean.valueOf(refactoringProperties.getProperty("newfilecreation", "false")).booleanValue();
		newfiles = separateNewFiles(refactoringProperties);
	}

	private String[] separateNewFiles(Properties refactoringProperties) {
		return String.valueOf(refactoringProperties.getProperty("newfiles", "")).replace(" ", "").split(",");
	}

	@Override
	protected void runTest() throws Throwable {
		MockToggleRefactoringTest refactoring = new MockToggleRefactoringTest(project.getFile(fileName), selection, cproject);
		if (newFileCreation) {
			pre_executeNewFileCreationRefactoring(refactoring);
			RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
			refactoring.getContext().setSettedDefaultAnswer(true);
			refactoring.getContext().setDefaultAnswer(true);
			if (fatalError) {
				assertConditionsFatalError(checkInitialConditions);
				return;
			}
			assertConditionsOk(checkInitialConditions);
			aftertest(refactoring);
			return;
		} else {
			RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
			if (fatalError) {
				assertConditionsFatalError(checkInitialConditions);
				return;
			}
			assertConditionsOk(checkInitialConditions);
			executeRefactoring(refactoring);
		}
	}

	private void aftertest(Refactoring refactoring) throws Exception {
		Change changes = refactoring.createChange(NULL_PROGRESS_MONITOR);
		assertConditionsOk(refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR));
		changes.perform(NULL_PROGRESS_MONITOR);
		filesDoExist();
		for (String fileName: fileMap.keySet()) {			
			IFile iFile = project.getFile(new Path(fileName));
			String code = getCodeFromIFile(iFile);
			String expectedSource = fileMap.get(fileName).getExpectedSource();
			assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(code));
		}
	}

	private void pre_executeNewFileCreationRefactoring(Refactoring refactoring) throws Exception {
		removeFiles();
		filesDoNotExist();
	}

	private void filesDoExist() {
		for (String fileName: newfiles) {
			IFile file = project.getFile(new Path(fileName));
			assertTrue(file.exists());
		}
	}

	private void filesDoNotExist() {
		for (String fileName: newfiles) {
			IFile file = project.getFile(new Path(fileName));
			assertFalse(file.exists());
		}
	}

	private void removeFiles() throws CoreException {
		for (String fileName: newfiles) {
			IFile file = project.getFile(new Path(fileName));
			file.delete(true, NULL_PROGRESS_MONITOR);
		}
	}

	private void executeRefactoring(Refactoring refactoring) throws Exception {
		Change changes = refactoring.createChange(NULL_PROGRESS_MONITOR);
		assertConditionsOk(refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR));
		changes.perform(NULL_PROGRESS_MONITOR);
		compareFiles(fileMap);		
	}
}
