/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

/**
 * This class contains the patter strings for a text widget
 *
 * @since 4.0
 */
public class IPatternMatchingTable {

	public static final String TEXT = "Text"; //$NON-NLS-1$
	public static final String FREETEXT = "FreeText"; //$NON-NLS-1$
	public static final String FILENAME = "FileName"; //$NON-NLS-1$
	public static final String TEXTPATTERNVALUE = "[A-Za-z0-9\\!\\?\\.: ]*"; //$NON-NLS-1$
	public static final String FREETEXTPATTERNVALUE = "[A-Za-z0-9() \\.\\s]*"; //$NON-NLS-1$
	public static final String FILEPATTERNVALUE = "([A-Za-z][:])?[[\\|\\\\|/]?[_!@#\\$%\\^()\\-+{}\\[\\]=;',A-Za-z0-9\\. ]*]*"; //$NON-NLS-1$
}
