/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Tom Ball (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoring;

/**
 * Test harness for Extract Local Variable refactoring tests.
 * 
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoringTest extends RefactoringTest {
	protected String variableName;

	public ExtractLocalVariableRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile file = project.getFile(fileName);
		ICElement element = CoreModel.getDefault().create(file);
		ExtractLocalVariableRefactoring refactoring =
				new ExtractLocalVariableRefactoring(element, selection, cproject);
		refactoring.getRefactoringInfo().setName(variableName);
		executeRefactoring(refactoring);		
		compareFiles(fileMap);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		variableName = refactoringProperties.getProperty("variablename", "temp"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
