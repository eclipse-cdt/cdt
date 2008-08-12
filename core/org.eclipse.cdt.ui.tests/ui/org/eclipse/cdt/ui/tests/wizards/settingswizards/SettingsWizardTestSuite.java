/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.wizards.settingswizards;

import junit.framework.TestSuite;

public class SettingsWizardTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new SettingsWizardTestSuite();
    }
    
    public SettingsWizardTestSuite() {
        super(SettingsWizardTestSuite.class.getName());
        addTestSuite(SettingsImportExportTest.class);
    }
}
