/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.core.search.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchTestSuite extends TestCase {
	public static Test suite() { 
			TestSuite suite= new TestSuite(SearchTestSuite.class.getName());
			 
			suite.addTestSuite(ClassDeclarationPatternTests.class);
			//suite.addTestSuite(FunctionMethodPatternTests.class);
			suite.addTestSuite(OtherPatternTests.class);
			suite.addTestSuite(ParseTestOnSearchFiles.class);
			return suite;
		}
}
