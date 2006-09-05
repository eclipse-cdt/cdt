/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.callhierarchy.CallHierarchyTestSuite;
import org.eclipse.cdt.ui.tests.text.TextTestSuite;
import org.eclipse.cdt.ui.tests.text.contentassist.ContentAssistTestSuite;
import org.eclipse.cdt.ui.tests.viewsupport.ViewSupportTestSuite;

/**
 * Test all areas of the UI.
 */
public class AutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() {
		
		// tests from package org.eclipse.cdt.ui.tests.text
		addTest(TextTestSuite.suite());

		// tests for package org.eclipse.cdt.ui.tests.viewsupport
		addTest(ViewSupportTestSuite.suite());

		// tests for package org.eclipse.cdt.ui.tests.callhierarchy
		addTest(CallHierarchyTestSuite.suite());
		
		// tests from package org.eclipse.cdt.ui.tests.text.contentAssist
		addTest(ContentAssistTestSuite.suite());

		// tests from package org.eclipse.cdt.ui.tests.text.contentAssist2
		// commented out because they are failing pretty badly
		// addTest(ContentAssist2TestSuite.suite());

		// tests from package org.eclipse.cdt.ui.tests.text.selection
		// commented out because they are failing pretty badly
		// addTest(SelectionTestSuite.suite());
	}
	
}
