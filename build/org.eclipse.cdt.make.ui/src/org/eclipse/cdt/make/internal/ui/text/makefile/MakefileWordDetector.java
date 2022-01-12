/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.jface.text.rules.IWordDetector;

public class MakefileWordDetector implements IWordDetector {

	//private static final String correctStartSpecChars = "%*().><"; //$NON-NLS-1$
	private static final String correctStartSpecChars = "%*()><"; //$NON-NLS-1$
	private static final String correctSpecChars = "@$/\\_"; //$NON-NLS-1$

	/**
	 * @see IWordDetector#isWordPart(char)
	 */
	@Override
	public boolean isWordPart(char character) {
		return Character.isLetterOrDigit(character) || (correctSpecChars.indexOf(character) >= 0);
	}

	/**
	 * @see IWordDetector#isWordStart(char)
	 */
	@Override
	public boolean isWordStart(char character) {
		return Character.isLetterOrDigit(character) || (correctStartSpecChars.indexOf(character) >= 0);
	}

}
