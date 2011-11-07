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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTester;

public class ToggleRefactoringTestSuite extends TestSuite {

	@SuppressWarnings("nls")
	public static Test suite() throws Exception {
		TestSuite suite = new ToggleRefactoringTestSuite();
		suite.addTest(RefactoringTester.suite("NewCreationTest",
				"resources/refactoring/NewCreationTest.rts"));
		suite.addTest(RefactoringTester.suite("ToggleErrorRefactoring",
				"resources/refactoring/ToggleErrorRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleSelectionTest",
				"resources/refactoring/ToggleSelection.rts"));
		suite.addTest(RefactoringTester.suite(
				"ToggleSimpleFunctionRefactoringTest",
				"resources/refactoring/ToggleSimpleFunctionRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleTemplateRefactoringTest",
				"resources/refactoring/ToggleTemplateRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleNamespaceRefactoringTest",
				"resources/refactoring/ToggleNamespaceRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleTryCatchRefactoringTest",
				"resources/refactoring/ToggleTryCatchRefactoring.rts"));
		suite.addTest(RefactoringTester.suite(
				"ToggleDefaultParameterRefactoringTest",
				"resources/refactoring/ToggleDefaultParameterRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleCtorDtorRefactoringTest",
				"resources/refactoring/ToggleCtorDtorRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleNestedRefactoringTest",
				"resources/refactoring/ToggleNestedRefactoring.rts"));
		suite.addTest(RefactoringTester.suite("ToggleDifferentSelectionsTest",
				"resources/refactoring/ToggleDifferentSelections.rts"));
		suite.addTest(RefactoringTester.suite("ToggleFreeFunctionTest",
				"resources/refactoring/ToggleFreeFunction.rts"));
		suite.addTest(RefactoringTester.suite("ToggleVirtualFunctionTest",
				"resources/refactoring/ToggleVirtualFunction.rts"));
		suite.addTest(RefactoringTester.suite("ToggleOrderingTest",
				"resources/refactoring/ToggleOrdering.rts"));
		suite.addTest(RefactoringTester.suite("ToggleCommentsClassToHeader",
				"resources/refactoring/ToggleCommentsClassToHeader.rts"));
		suite.addTest(RefactoringTester.suite("ToggleCommentsHeaderToClass",
				"resources/refactoring/ToggleCommentsHeaderToClass.rts"));
		suite.addTest(RefactoringTester.suite("ToggleCommentsHeaderToImpl",
				"resources/refactoring/ToggleCommentsHeaderToImpl.rts"));
		suite.addTest(RefactoringTester.suite("ToggleCommentsImplToHeader",
				"resources/refactoring/ToggleCommentsImplToHeader.rts"));
		suite.addTestSuite(ToggleNodeHelperTest.class);
		return suite;
	}
}
