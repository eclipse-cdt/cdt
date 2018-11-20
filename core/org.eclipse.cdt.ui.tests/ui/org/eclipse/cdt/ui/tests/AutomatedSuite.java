/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 * Markus Schorn (Wind River Systems)
 * Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all areas of the UI.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ org.eclipse.cdt.ui.tests.text.TextTestSuite.class,
		org.eclipse.cdt.ui.tests.outline.OutlineTestSuite.class,
		org.eclipse.cdt.ui.tests.viewsupport.ViewSupportTestSuite.class,
		org.eclipse.cdt.ui.tests.callhierarchy.CallHierarchyTestSuite.class,
		org.eclipse.cdt.ui.tests.callhierarchy.extension.CHExtensionTest.class,
		org.eclipse.cdt.ui.tests.typehierarchy.TypeHierarchyTestSuite.class,
		org.eclipse.cdt.ui.tests.includebrowser.IncludeBrowserTestSuite.class,
		org.eclipse.cdt.ui.tests.text.contentassist.ContentAssistTestSuite.class,
		org.eclipse.cdt.ui.tests.text.contentassist2.ContentAssist2TestSuite.class,
		org.eclipse.cdt.ui.tests.text.selection.SelectionTestSuite.class,
		org.eclipse.cdt.ui.tests.quickfix.AssistQuickFixTest.class,
		org.eclipse.cdt.ui.tests.buildconsole.BuildConsoleTests.class,
		org.eclipse.cdt.ui.tests.search.SearchTestSuite.class,
		org.eclipse.cdt.ui.tests.refactoring.RefactoringTestSuite.class, org.eclipse.cdt.ui.tests.chelp.CHelpTest.class,
		org.eclipse.cdt.ui.tests.wizards.classwizard.ClassWizardTestSuite.class,
		org.eclipse.cdt.ui.tests.wizards.settingswizards.SettingsWizardTestSuite.class,
		org.eclipse.cdt.ui.tests.misc.MiscTestSuite.class, org.eclipse.cdt.ui.tests.editor.EditorTestSuite.class,
		org.eclipse.cdt.ui.tests.templateengine.AllTemplateEngineTests.class,

})
public class AutomatedSuite {
}
