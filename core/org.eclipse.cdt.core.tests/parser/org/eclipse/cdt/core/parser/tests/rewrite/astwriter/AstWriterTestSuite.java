/*******************************************************************************
 * Copyright (c) 2006, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf
 * @deprecated In preparation for moving to JUnit5 test suites are deprecated. See Bug 569839
 */
@Deprecated
public class AstWriterTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("AstWriterTestSuite");
		suite.addTest(SourceRewriteTest.suite());
		suite.addTestSuite(ExpressionWriterTest.class);
		return suite;
	}
}
