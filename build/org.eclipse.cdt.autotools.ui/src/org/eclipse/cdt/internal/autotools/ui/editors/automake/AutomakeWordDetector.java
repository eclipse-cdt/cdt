/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.jface.text.rules.IWordDetector;

public class AutomakeWordDetector implements IWordDetector {

	private static final String correctStartSpecChars = "$%*().><"; //$NON-NLS-1$
	private static final String correctSpecChars = "?@$/\\<*%"; //$NON-NLS-1$

	@Override
	public boolean isWordStart(char character) {
		return Character.isLetterOrDigit(character) || (correctStartSpecChars.indexOf(character) >= 0);
	}

	@Override
	public boolean isWordPart(char character) {
		return Character.isLetterOrDigit(character) || (correctSpecChars.indexOf(character) >= 0);
	}
}
