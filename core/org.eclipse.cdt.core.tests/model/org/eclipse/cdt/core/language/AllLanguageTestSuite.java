/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author crecoskie
 * @deprecated In preparation for moving to JUnit5 test suites are deprecated. See Bug 569839
 */
@Deprecated
public class AllLanguageTestSuite extends TestSuite {
	public static Test suite() {
		TestSuite suite = new AllLanguageTestSuite();

		suite.addTest(LanguageInheritanceTests.suite());

		return suite;
	}
}
