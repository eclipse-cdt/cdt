/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.tests.prefix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CompletionTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(CompletionTestSuite.class.getName());
		suite.addTestSuite(BasicCompletionTest.class);
		return suite;
	}
}
