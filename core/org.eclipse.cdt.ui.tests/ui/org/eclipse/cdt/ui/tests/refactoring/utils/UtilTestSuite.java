/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class UtilTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		UtilTestSuite suite = new UtilTestSuite();
		suite.addTest(IdentifierHelperTest.suite());
		suite.addTestSuite(DefinitionFinderTest.class);
		suite.addTestSuite(PseudoNameGeneratorTest.class);
		suite.addTestSuite(NameComposerTest.class);
		return suite;
	}
}
