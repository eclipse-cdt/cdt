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
package org.eclipse.cdt.ui.tests.refactoring.extractconstant;

import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantInfo;
import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Emanuel Graf
 */
public class ExtractConstantRefactoringTest extends RefactoringTest {


	protected VisibilityEnum visibility;

	public ExtractConstantRefactoringTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileName);
		ExtractConstantInfo info = new ExtractConstantInfo();
		ExtractConstantRefactoring refactoring = new ExtractConstantRefactoring( refFile, selection, info);
		try {
			refactoring.lockIndex();
			RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
			assertConditionsOk(checkInitialConditions);
			info.setName("theAnswer"); //$NON-NLS-1$
			info.setVisibility(visibility);
			Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
			RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
			assertConditionsOk(finalConditions);
			createChange.perform(NULL_PROGRESS_MONITOR);
		}finally {
			refactoring.unlockIndex();
		}
		compareFiles(fileMap);

	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		visibility = VisibilityEnum.getEnumForStringRepresentation(refactoringProperties.getProperty("visibility", VisibilityEnum.v_public.toString())); //$NON-NLS-1$
	
	}

}
