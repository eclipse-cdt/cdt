/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

public interface ITerminalConstants {

	public static final String  PREF_HAS_MIGRATED      = "TerminalPref.migrated"; //$NON-NLS-1$

	public static final String  PREF_BUFFERLINES       = "TerminalPrefBufferLines"; //$NON-NLS-1$
	public static final String  PREF_INVERT_COLORS     = "TerminalPrefInvertColors"; //$NON-NLS-1$
	public static final int     DEFAULT_BUFFERLINES    = 1000;
	public static final boolean DEFAULT_INVERT_COLORS  = false;

	public static final String  FONT_DEFINITION = "terminal.views.view.font.definition"; //$NON-NLS-1$

}
