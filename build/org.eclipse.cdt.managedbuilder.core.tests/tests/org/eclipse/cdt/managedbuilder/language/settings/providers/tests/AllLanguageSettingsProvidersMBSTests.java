/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import junit.framework.TestSuite;

/**
 * Test suite to test language settings providers defined in cdt.managedbuilder.core.
 */
public class AllLanguageSettingsProvidersMBSTests extends TestSuite {

	public static TestSuite suite() {
		return new AllLanguageSettingsProvidersMBSTests();
	}

	public AllLanguageSettingsProvidersMBSTests() {
		super(AllLanguageSettingsProvidersMBSTests.class.getName());

		addTestSuite(LanguageSettingsProvidersMBSTest.class);
		addTestSuite(GCCBuildCommandParserTest.class);
		addTestSuite(BuiltinSpecsDetectorTest.class);
		addTestSuite(GCCBuiltinSpecsDetectorTest.class);
	}
}
