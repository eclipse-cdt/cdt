/*******************************************************************************
 * Copyright (c) 2010, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language.settings.providers;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite to cover core Language Settings Providers functionality.
 */
public class AllLanguageSettingsProvidersCoreTest {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllLanguageSettingsProvidersCoreTest.class.getName());

		suite.addTest(LanguageSettingsExtensionsTest.suite());
		suite.addTest(LanguageSettingsManagerTest.suite());
		suite.addTest(LanguageSettingsSerializableProviderTest.suite());
		suite.addTest(LanguageSettingsPersistenceProjectTest.suite());
		suite.addTest(LanguageSettingsListenersTest.suite());
		suite.addTest(LanguageSettingsScannerInfoProviderTest.suite());
		suite.addTest(LanguageSettingsProviderReferencedProjectsTest.suite());
		return suite;
	}
}
