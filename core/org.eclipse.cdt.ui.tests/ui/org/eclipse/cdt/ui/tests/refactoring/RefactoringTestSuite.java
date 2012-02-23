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
 *     Tom Ball (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.extractconstant.ExtractConstantRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.extractfunction.ExtractFunctionRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable.ExtractLocalVariableRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.gettersandsetters.GenerateGettersAndSettersTest;
import org.eclipse.cdt.ui.tests.refactoring.hidemethod.HideMethodRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.implementmethod.ImplementMethodRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.rename.RenameRegressionTests;
import org.eclipse.cdt.ui.tests.refactoring.togglefunction.ToggleRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.utils.UtilTestSuite;

/**
 * @author Emanuel Graf
 */
public class RefactoringTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new RefactoringTestSuite();
		suite.addTest(UtilTestSuite.suite());
		suite.addTest(RenameRegressionTests.suite());
		suite.addTest(ExtractFunctionRefactoringTest.suite());
		suite.addTest(ExtractConstantRefactoringTest.suite());
		suite.addTest(HideMethodRefactoringTest.suite());
		suite.addTest(GenerateGettersAndSettersTest.suite());
		suite.addTest(ImplementMethodRefactoringTest.suite());
		suite.addTest(ExtractLocalVariableRefactoringTest.suite());
		suite.addTest(ToggleRefactoringTest.suite());
		return suite;
	}
}
