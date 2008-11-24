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
package org.eclipse.cdt.ui.tests.refactoring;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.extractfunction.ExtractFunctionTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable.ExtractLocalVariableTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.rename.RenameRegressionTests;
import org.eclipse.cdt.ui.tests.refactoring.utils.UtilTestSuite;

/**
 * @author Emanuel Graf
 *
 */
public class RefactoringTestSuite extends TestSuite {
	
	public static Test suite() throws Exception {
		TestSuite suite = new RefactoringTestSuite();
		suite.addTest(UtilTestSuite.suite());
		suite.addTest(RenameRegressionTests.suite());
		suite.addTest(ExtractFunctionTestSuite.suite());
		suite.addTest(RefactoringTester.suite("ExtractConstantRefactoringTests", "resources/refactoring/ExtractConstant.rts"));
		suite.addTest(RefactoringTester.suite("HideMethodRefactoringTests", "resources/refactoring/HideMethod.rts"));
		suite.addTest(RefactoringTester.suite("GettersAndSettersTests", "resources/refactoring/GenerateGettersAndSetters.rts"));
		suite.addTest(RefactoringTester.suite("ImplementMethodRefactoringTests", "resources/refactoring/ImplementMethod.rts"));
		suite.addTest(ExtractLocalVariableTestSuite.suite());
		return suite;
	}
}
