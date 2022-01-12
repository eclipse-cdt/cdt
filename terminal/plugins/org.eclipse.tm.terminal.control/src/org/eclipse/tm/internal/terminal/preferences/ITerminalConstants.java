/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

import org.eclipse.tm.terminal.model.TerminalColor;

/**
 * Constants for Terminal Preferences.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITerminalConstants {

	public static final String FONT_DEFINITION = "terminal.views.view.font.definition"; //$NON-NLS-1$
	public static final String PREF_HAS_MIGRATED = "TerminalPref.migrated"; //$NON-NLS-1$

	public static final String PREF_BUFFERLINES = "TerminalPrefBufferLines"; //$NON-NLS-1$
	public static final String PREF_INVERT_COLORS = "TerminalPrefInvertColors"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String PREF_FONT_DEFINITION = "TerminalFontDefinition"; //$NON-NLS-1$
	public static final int DEFAULT_BUFFERLINES = 1000;
	public static final boolean DEFAULT_INVERT_COLORS = false;
	/**
	 * @since 5.0
	 */
	public static final String DEFAULT_FONT_DEFINITION = FONT_DEFINITION;

	/**
	 * @since 5.0
	 */
	public static String getPrefForTerminalColor(TerminalColor tc) {
		return tc.toString();
	}

}
