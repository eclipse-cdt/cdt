/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCProjectDescriptionTests {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AllCProjectDescriptionTests.class.getName());

        // Just add more test cases here as you create them for
        // each class being tested
		suite.addTest(CConfigurationDescriptionReferenceTests.suite());
		suite.addTest(CConfigurationDescriptionExportSettings.suite());
		suite.addTest(ExternalSettingsProviderTests.suite());
		suite.addTest(CfgSettingsTests.suite());
		suite.addTest(CProjectDescriptionDeltaTests.suite());
		suite.addTest(ProjectCreationStateTests.suite());
		suite.addTest(BackwardCompatibilityTests.suite());
		suite.addTest(CProjectDescriptionBasicTests.suite());
		suite.addTest(CProjectDescriptionStorageTests.suite());
        return suite;
    }
}
