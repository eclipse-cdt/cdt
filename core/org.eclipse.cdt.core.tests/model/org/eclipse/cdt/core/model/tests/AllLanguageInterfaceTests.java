/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * LanguageInterfaceTests
 * lists all parts of the C/C++ language interface objects
 * to be tested.
 * @author bnicolle
 *
 */
public class AllLanguageInterfaceTests {

	/**
	 * 
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AllLanguageInterfaceTests.class.getName());

		// Just add more test cases here as you create them for 
		// each class being tested
        
		suite.addTest(IIncludeTests.suite());
		suite.addTest(IMacroTests.suite());
		suite.addTest(StructuralMacroTests.suite());
		suite.addTest(IStructureTests.suite());
		suite.addTest(StructuralStructureTests.suite());		
		suite.addTest(ITemplateTests.suite());
		suite.addTest(StructuralTemplateTests.suite());		
		return suite;
        
	}

}
