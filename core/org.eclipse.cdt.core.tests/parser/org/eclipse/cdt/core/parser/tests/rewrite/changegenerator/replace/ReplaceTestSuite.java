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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class ReplaceTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("ReplaceTestSuite");
	
		suite.addTest(ArrayModifierTest.suite());
		suite.addTest(ArraySizeExpressionTest.suite());
		suite.addTest(CtorChainInitializerTest.suite());
		suite.addTest(ExceptionTest.suite());
		suite.addTest(ExpressionTest.suite());
		suite.addTest(IdenticalTest.suite());
		suite.addTest(InitializerTest.suite());
		suite.addTest(MoveRenameTest.suite());
		suite.addTest(MoveTest.suite());
		suite.addTest(MultilineWhitespaceHandlingTest.suite());
		suite.addTest(NameTest.suite());
		suite.addTest(NestedReplaceTest.suite());
		suite.addTest(NewInitializerExpressionTest.suite());
		suite.addTest(PointerInParameterTest.suite());
		suite.addTest(ReplaceForLoopBodyTest.suite());
		suite.addTest(ReplaceInsertStatementTest.suite());
		suite.addTest(SameNameTest.suite());
		suite.addTest(StatementTest.suite());
		suite.addTest(WhitespaceHandlingTest.suite());
		return suite;
	}
}
