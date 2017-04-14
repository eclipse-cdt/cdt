/*******************************************************************************
 * Copyright (c) 2015, 2017 Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial implementation
 *     Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for functionality in the package org.eclipse.cdt.internal.ui.editor. 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	SourceHeaderPartnerFinderTest.class,

})
public class EditorTestSuite {
}
