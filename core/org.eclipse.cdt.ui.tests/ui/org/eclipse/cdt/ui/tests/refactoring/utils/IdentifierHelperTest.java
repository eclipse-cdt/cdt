/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 * 
 */
public class IdentifierHelperTest extends TestSuite {

	public IdentifierHelperTest() {
		super("Identifier Helper Test"); //$NON-NLS-1$
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Identifier Helper"); //$NON-NLS-1$
		suite.addTest(new CorrectCaseTest());
		suite.addTest(new DigitFirstCaseTest());
		suite.addTest(new EmptyCaseTest());
		suite.addTest(new IllegalCharCaseTest());
		suite.addTest(new KeywordCaseTest());
		suite.addTestSuite(NameHelperTest.class);
		return suite;
	}
}
