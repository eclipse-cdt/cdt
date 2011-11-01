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
 *     Tom Ball (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.extractconstant.ExtractConstantTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.extractfunction.ExtractFunctionTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable.ExtractLocalVariableTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.gettersandsetters.GenerateGettersAndSettersTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.hidemethod.HideMethodTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.implementmethod.ImplementMethodTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.rename.RenameRegressionTests;
import org.eclipse.cdt.ui.tests.refactoring.togglefunction.ToggleRefactoringTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.utils.UtilTestSuite;

/**
 * @author Emanuel Graf
 */
public class RefactoringTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new RefactoringTestSuite();
		suite.addTest(UtilTestSuite.suite());
		suite.addTest(RenameRegressionTests.suite());
		suite.addTest(ExtractFunctionTestSuite.suite());
		suite.addTest(ExtractConstantTestSuite.suite());
		suite.addTest(HideMethodTestSuite.suite());
		suite.addTest(GenerateGettersAndSettersTestSuite.suite());
		suite.addTest(ImplementMethodTestSuite.suite());
		suite.addTest(ExtractLocalVariableTestSuite.suite());
		suite.addTest(ToggleRefactoringTestSuite.suite());
		return suite;
	}
}
