/*******************************************************************************
 * Copyright (c) 2008, 2017 Institute for Software, HSR Hochschule fuer Technik
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
 *     Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.eclipse.cdt.ui.tests.refactoring.extractconstant.ExtractConstantRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.extractfunction.ExtractFunctionRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable.ExtractLocalVariableRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.gettersandsetters.GenerateGettersAndSettersTest;
import org.eclipse.cdt.ui.tests.refactoring.hidemethod.HideMethodRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.implementmethod.ImplementMethodRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.includes.IncludesTestSuite;
import org.eclipse.cdt.ui.tests.refactoring.rename.RenameRegressionTests;
import org.eclipse.cdt.ui.tests.refactoring.togglefunction.ToggleRefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.utils.UtilTestSuite;

/**
 * @author Emanuel Graf
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	UtilTestSuite.class,
	RenameRegressionTests.class,
	ExtractFunctionRefactoringTest.class,
	ExtractConstantRefactoringTest.class,
	HideMethodRefactoringTest.class,
	GenerateGettersAndSettersTest.class,
	ImplementMethodRefactoringTest.class,
	ExtractLocalVariableRefactoringTest.class,
	ToggleRefactoringTest.class,
	IncludesTestSuite.class,

})
public class RefactoringTestSuite {
}
