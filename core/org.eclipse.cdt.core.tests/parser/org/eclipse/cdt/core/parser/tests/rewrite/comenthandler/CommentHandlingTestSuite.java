/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
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
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.comenthandler;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Guido Zgraggen IFS
 * @deprecated In preparation for moving to JUnit5 test suites are deprecated. See Bug 569839
 */
@Deprecated
public class CommentHandlingTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(CommentHandlingTestSuite.class.getName());
		suite.addTest(CommentHandlingTest.suite());
		suite.addTest(CommentHandlingWithRewriteTest.suite());
		suite.addTestSuite(NodeCommentMapTest.class);
		return suite;
	}
}
