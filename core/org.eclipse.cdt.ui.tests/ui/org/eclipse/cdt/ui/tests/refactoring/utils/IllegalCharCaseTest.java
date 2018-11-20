/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
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
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;

import junit.framework.TestCase;

/**
 * @author Thomas Corbat
 *
 */
public class IllegalCharCaseTest extends TestCase {

	public IllegalCharCaseTest() {
		super("Check Illegal Character Identifier"); //$NON-NLS-1$
	}

	@Override
	public void runTest() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName("a~"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

		result = IdentifierHelper.checkIdentifierName("a%"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

		result = IdentifierHelper.checkIdentifierName("a!"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

		result = IdentifierHelper.checkIdentifierName("{}"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

	}

}
