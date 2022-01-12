/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IncludesTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		IncludesTestSuite suite = new IncludesTestSuite();
		suite.addTestSuite(IncludeMapTest.class);
		suite.addTest(BindingClassifierTest.suite());
		suite.addTest(IncludeOrganizerTest.suite());
		return suite;
	}
}
