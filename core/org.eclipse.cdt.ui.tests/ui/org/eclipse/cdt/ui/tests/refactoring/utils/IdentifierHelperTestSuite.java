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
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Thomas Corbat
 */
public class IdentifierHelperTestSuite extends TestSuite {

	public IdentifierHelperTestSuite() {
		super("Identifier Helper Test"); //$NON-NLS-1$
	}

	public static Test suite() {
		return new TestSuite(CorrectCaseTest.class, DigitFirstCaseTest.class, EmptyCaseTest.class,
				IllegalCharCaseTest.class, KeywordCaseTest.class);
	}
}
