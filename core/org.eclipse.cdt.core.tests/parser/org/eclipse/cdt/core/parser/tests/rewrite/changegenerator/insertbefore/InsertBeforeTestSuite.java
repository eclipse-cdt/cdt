/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class InsertBeforeTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("InsertBeforeTestSuite");

		suite.addTest(FirstParameterTest.suite());
		suite.addTest(PointerParameterTest.suite());
		suite.addTest(ExceptionTest.suite());
		suite.addTest(CtorChainInitializerTest.suite());
		suite.addTest(ArrayModifierTest.suite());
		suite.addTest(ExpressionTest.suite());
		suite.addTest(ArraySizeExpressionTest.suite());
		suite.addTest(AddDeclarationBugTest.suite());
		suite.addTest(MultilineWhitespaceHandlingTest.suite());
		suite.addTest(SelfInsertionTest.suite());
		suite.addTest(InsertStatementTest.suite());
		suite.addTest(InsertMultipleStatementsTest.suite());

		return suite;
	}
}
