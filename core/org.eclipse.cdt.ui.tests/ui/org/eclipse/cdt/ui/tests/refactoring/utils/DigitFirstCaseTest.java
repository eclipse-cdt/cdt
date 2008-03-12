/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;

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
