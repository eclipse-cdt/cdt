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
public class DigitFirstCaseTest extends TestCase {

	public DigitFirstCaseTest() {
		super("Check Digit First Identifier"); //$NON-NLS-1$
	}

	@Override
	public void runTest() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName("0"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

		result = IdentifierHelper.checkIdentifierName("9"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

		result = IdentifierHelper.checkIdentifierName("0a"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

		result = IdentifierHelper.checkIdentifierName("99"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

	}

}
