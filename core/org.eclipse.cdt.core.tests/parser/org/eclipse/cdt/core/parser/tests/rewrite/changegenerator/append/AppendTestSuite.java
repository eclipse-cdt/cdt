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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.append;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class AppendTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("ChangeGenerator Append Child Tests");
		suite.addTest(ParameterTest.suite());
		suite.addTest(ParameterToListTest.suite());
		suite.addTest(PointerToParameterTest.suite());
		suite.addTest(PointerToPointerParameterTest.suite());
		suite.addTest(ExceptionTest.suite());
		suite.addTest(CtorChainInitializerTest.suite());
		suite.addTest(ArrayModifierTest.suite());
		suite.addTest(ExpressionTest.suite());
		suite.addTest(ArraySizeExpressionTest.suite());
		return suite;
	}
}
