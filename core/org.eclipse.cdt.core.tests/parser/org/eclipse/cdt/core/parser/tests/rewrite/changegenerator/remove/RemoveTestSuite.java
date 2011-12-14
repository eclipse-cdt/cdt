/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.remove;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class RemoveTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("ChangeGenerator Remove Tests");
		suite.addTest(DeclarationTest.suite());
		suite.addTest(FirstParameterTest.suite());
		suite.addTest(MiddleParameterTest.suite());
		suite.addTest(LastParameterTest.suite());
		suite.addTest(SingleParameterTest.suite());
		suite.addTest(PointerInParameterTest.suite());
		suite.addTest(ExceptionTest.suite());
		suite.addTest(CtorChainInitializerTest.suite());
		suite.addTest(ArrayModifierTest.suite());
		suite.addTest(ExpressionTest.suite());
		suite.addTest(ArraySizeExpressionTest.suite());
		suite.addTest(NewInitializerExpressionTest.suite());
		suite.addTest(StatementTest.suite());
		return suite;
	}
}
