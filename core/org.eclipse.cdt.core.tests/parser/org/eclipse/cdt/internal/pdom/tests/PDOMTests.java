/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMTests extends TestSuite {

	public static Test suite() {
		TestSuite suite = new PDOMTests();
		
		suite.addTest(EnumerationTests.suite());
		suite.addTest(ClassTests.suite());
		suite.addTest(TypesTests.suite());
		suite.addTest(IncludesTests.suite());
		suite.addTest(BTreeTests.suite());

		suite.addTest(CPPFieldTests.suite());
		suite.addTest(CPPFunctionTests.suite());
		suite.addTest(CPPVariableTests.suite());
		suite.addTest(MethodTests.suite());

		suite.addTest(CFunctionTests.suite());
		suite.addTest(CVariableTests.suite());
		return suite;
	}
	
}
