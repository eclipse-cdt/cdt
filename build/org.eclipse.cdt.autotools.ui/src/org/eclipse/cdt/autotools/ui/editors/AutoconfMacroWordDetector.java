/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.rules.IWordDetector;

public class AutoconfMacroWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		return ((Character.isLetter(c) && Character.isUpperCase(c)) || 
				Character.isDigit(c) ||	c == '_');
	}

	public boolean isWordStart(char c) {
		return (c == 'A');
	}

}
