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
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class ChangeGeneratorTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(ChangeGeneratorTestSuite.class.getName());

		suite.addTest(AppendTests.suite());
		suite.addTest(InsertBeforeTests.suite());
		suite.addTest(RemoveTests.suite());
		suite.addTest(ReplaceTests.suite());

		return suite;
	}
}
