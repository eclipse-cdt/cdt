/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.cdt.ui.tests.text.TextTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all areas of the UI.
 */
public class AutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() throws Exception {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() throws Exception {
		
		// tests from package org.eclipse.cdt.ui.tests.text
		addTest(TextTestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.outline
//		addTest(OutlineTestSuite.suite());
//
//		// tests for package org.eclipse.cdt.ui.tests.viewsupport
//		addTest(ViewSupportTestSuite.suite());
//
//		// tests for package org.eclipse.cdt.ui.tests.callhierarchy
//		addTest(CallHierarchyTestSuite.suite());
//
//		// tests for package org.eclipse.cdt.ui.tests.typehierarchy
//		addTest(TypeHierarchyTestSuite.suite());
//
//		// tests for package org.eclipse.cdt.ui.tests.includebrowser
//		addTest(IncludeBrowserTestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.text.contentAssist
//		addTest(ContentAssistTestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.text.contentAssist2
//		addTest(ContentAssist2TestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.text.selection
//		addTest(SelectionTestSuite.suite());
//		
//		// tests from package org.eclipse.cdt.ui.tests.quickfix
//		addTest(AssistQuickFixTest.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.buildconsole
//		addTest(BuildConsoleTests.suite());
//		
//		// tests from package org.eclipse.cdt.ui.tests.search
//		addTest(SearchTestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.refactoring
//		addTest(RefactoringTestSuite.suite());
//		
//		// tests from package org.eclipse.cdt.ui.tests.chelp
//		addTest(CHelpTest.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.wizards.classwizard
//		addTest(ClassWizardTestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.wizards.settingswizards
//		addTest(SettingsWizardTestSuite.suite());
//
//		// tests from package org.eclipse.cdt.ui.tests.misc
//		addTest(MiscTestSuite.suite());
//		
//		// tests from package org.eclipse.cdt.ui.tests.editor
//		addTest(EditorTestSuite.suite());
//
//		addTest(AllTemplateEngineTests.suite());
	}
}
