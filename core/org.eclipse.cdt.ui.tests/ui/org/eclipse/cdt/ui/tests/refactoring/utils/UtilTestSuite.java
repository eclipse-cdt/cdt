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
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTester;

/**
 * @author Thomas Corbat
 */
public class UtilTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		UtilTestSuite suite = new UtilTestSuite(); 
		suite.addTest(IdentifierHelperTest.suite());
		suite.addTest(RefactoringTester.suite("TranslationUnitHelperTest", "resources/refactoring/TranslationunitHelper.rts")); //$NON-NLS-1$ //$NON-NLS-2$
		suite.addTest(RefactoringTester.suite("DefinitionFinderTest", "resources/refactoring/DefinitionFinder.rts")); //$NON-NLS-1$ //$NON-NLS-2$
		suite.addTestSuite(PseudoNameGeneratorTest.class);
		return suite;
	}
}
