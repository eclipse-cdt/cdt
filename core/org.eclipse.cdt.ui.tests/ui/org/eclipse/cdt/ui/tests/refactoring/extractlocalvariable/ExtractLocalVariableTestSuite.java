/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTester;

/**
 * Test suite to run just the Extract Local Variable unit tests.
 * 
 * @author Tom Ball
 */
public class ExtractLocalVariableTestSuite extends TestSuite {
	@SuppressWarnings("nls")
	public static Test suite() throws Exception {
		TestSuite suite = new ExtractLocalVariableTestSuite();
		suite.addTest(RefactoringTester.suite("ExtractLocalVariableRefactoringTests", 
				"resources/refactoring/ExtractLocalVariable.rts"));
		suite.addTest(RefactoringTester.suite("ExtractLocalVariableRefactoringHistoryTests", 
				"resources/refactoring/ExtractLocalVariableHistory.rts"));
		return suite;
	}
}
