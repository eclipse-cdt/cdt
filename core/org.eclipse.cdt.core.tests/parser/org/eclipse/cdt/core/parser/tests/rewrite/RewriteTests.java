/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
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
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite;

import org.eclipse.cdt.core.parser.tests.rewrite.astwriter.AstWriterTestSuite;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTestSuite;
import org.eclipse.cdt.core.parser.tests.rewrite.comenthandler.CommentHandlingTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RewriteTests extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(RewriteTests.class.getName());
		suite.addTest(AstWriterTestSuite.suite());
		suite.addTest(CommentHandlingTestSuite.suite());
		suite.addTest(ChangeGeneratorTestSuite.suite());
		return suite;
	}
}
