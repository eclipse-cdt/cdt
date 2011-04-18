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
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.implementmethod;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.ImplementMethodRefactoring;
 
/**
 * @author Mirko Stocker
 */
public class ImplementMethodRefactoringTest extends RefactoringTest {
	protected int finalWarnings;
	private int initialWarnings;
	private int infos;

	public ImplementMethodRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileName);
		ICElement element = CoreModel.getDefault().create(refFile);
		CRefactoring2 refactoring = new ImplementMethodRefactoring(element, selection, cproject, astCache);
		RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);

		if (initialWarnings == 0) {
			assertConditionsOk(checkInitialConditions);
		} else {
			assertConditionsFatalError(checkInitialConditions, initialWarnings);
			return;
		}

		RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
		Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
		
		if (finalWarnings > 0) {
			assertConditionsWarning(finalConditions, finalWarnings);
		} else if (infos > 0) {
			assertConditionsInfo(finalConditions, infos);
		} else {
			assertConditionsOk(finalConditions);
		}
		
		createChange.perform(NULL_PROGRESS_MONITOR);
		compareFiles(fileMap);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		finalWarnings = new Integer(refactoringProperties.getProperty("finalWarnings", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
		initialWarnings = Integer.parseInt(refactoringProperties.getProperty("initialWarnings", "0"));  //$NON-NLS-1$//$NON-NLS-2$
		infos = Integer.parseInt(refactoringProperties.getProperty("infos", "0"));  //$NON-NLS-1$//$NON-NLS-2$
	}
}
