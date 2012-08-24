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
package org.eclipse.cdt.autotools.tests;

import org.eclipse.cdt.autotools.tests.autoconf.AutoconfTests;
import org.eclipse.cdt.autotools.tests.editors.EditorTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/** On Windows requires either Cygwin or MinGW to be in PATH */
public class AllAutotoolsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.cdt.autotools.core.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AutotoolsProjectTest0.class);
		suite.addTestSuite(AutotoolsProjectNatureTest.class);
		suite.addTestSuite(AutotoolsProjectTest1.class);
		suite.addTestSuite(AutotoolsProjectTest2.class);
		suite.addTestSuite(UpdateConfigureTest.class);
		suite.addTest(AutoconfTests.suite());
		suite.addTest(EditorTests.suite());
		//$JUnit-END$
		return suite;
	}

}
