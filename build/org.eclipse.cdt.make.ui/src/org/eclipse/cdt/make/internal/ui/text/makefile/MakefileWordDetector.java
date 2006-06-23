/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.jface.text.rules.IWordDetector;

public class MakefileWordDetector implements IWordDetector {

	private static final String correctStartSpecChars = "%*().><"; //$NON-NLS-1$
	private static final String correctSpecChars = "@$/\\"; //$NON-NLS-1$

	/**
	 * @see IWordDetector#isWordPart(character)
	 */
	public boolean isWordPart(char character) {
		return Character.isLetterOrDigit(character) || (correctSpecChars.indexOf(character) >= 0);
	}

	/**
	 * @see IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char character) {
		return Character.isLetterOrDigit(character) || (correctStartSpecChars.indexOf(character) >= 0);
	}

}
