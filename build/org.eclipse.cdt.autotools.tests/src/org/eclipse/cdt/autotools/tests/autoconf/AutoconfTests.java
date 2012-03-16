/*******************************************************************************
 * Copyright (c) 2008 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests.autoconf;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AutoconfTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.cdt.autotools.core.tests.autoconf");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestMacroParser.class);
		suite.addTestSuite(TestTokenizer.class);
		suite.addTestSuite(TestShellParser.class);
		//$JUnit-END$
		return suite;
	}

}
