/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
