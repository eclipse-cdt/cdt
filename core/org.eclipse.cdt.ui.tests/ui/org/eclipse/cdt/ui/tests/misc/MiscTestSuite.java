/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.misc;

import junit.framework.TestSuite;

/**
 * Test suite for miscellaneous tests which do not fit neatly to other
 * categories.
 */
public class MiscTestSuite extends TestSuite {

	public static TestSuite suite() {
		return new MiscTestSuite();
	}

	public MiscTestSuite() {
		super(MiscTestSuite.class.getName());
		addTestSuite(CDTSharedImagesTests.class);
		addTestSuite(LanguageVerifierTests.class);
	}
}
