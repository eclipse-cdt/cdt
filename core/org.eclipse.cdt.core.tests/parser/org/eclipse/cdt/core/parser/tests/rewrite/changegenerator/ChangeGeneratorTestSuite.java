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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.append.AppendTestSuite;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore.InsertBeforeTestSuite;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.remove.RemoveTestSuite;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace.ReplaceTestSuite;

/**
 * @author Thomas Corbat
 */
public class ChangeGeneratorTestSuite{

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("ChangeGeneratorTests");

		suite.addTest(ReplaceTestSuite.suite());
		suite.addTest(RemoveTestSuite.suite());
		suite.addTest(InsertBeforeTestSuite.suite());
		suite.addTest(AppendTestSuite.suite());

		return suite;
	}
}
