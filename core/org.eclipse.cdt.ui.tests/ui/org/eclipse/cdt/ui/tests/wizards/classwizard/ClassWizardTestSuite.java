/*******************************************************************************
 * Copyright (c) 2011 Stefan Ghiaus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Ghiaus - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.wizards.classwizard;

import junit.framework.TestSuite;

public class ClassWizardTestSuite extends TestSuite {
	
	public static TestSuite suite() {
        return new ClassWizardTestSuite();
    }
	
	public ClassWizardTestSuite() {
		super(ClassWizardTestSuite.class.getName());
		addTestSuite(ClassWizardNameTest.class);
	}
}
