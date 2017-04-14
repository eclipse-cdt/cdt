/*******************************************************************************
 * Copyright (c) 2010, 2017 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *     Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.misc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for miscellaneous tests which do not fit neatly to other
 * categories.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	CDTSharedImagesTests.class,
	LanguageVerifierTests.class,


})
public class MiscTestSuite {
}
