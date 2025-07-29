/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.impl;

import org.eclipse.osgi.util.NLS;

public class TerminalMessages extends NLS {
	static {
		NLS.initializeMessages(TerminalMessages.class.getName(), TerminalMessages.class);
	}

	public static String TerminalColorPresets_EclipseDark;
	public static String TerminalColorPresets_EclipseLight;
	public static String TerminalColorPresets_TerminalDefaults;
	public static String TerminalColorsFieldEditor_Background;
	public static String TerminalColorsFieldEditor_Black;
	public static String TerminalColorsFieldEditor_Blue;
	public static String TerminalColorsFieldEditor_BrightBlack;
	public static String TerminalColorsFieldEditor_BrightBlue;
	public static String TerminalColorsFieldEditor_BrightCyan;
	public static String TerminalColorsFieldEditor_BrightGreen;
	public static String TerminalColorsFieldEditor_BrightMagenta;
	public static String TerminalColorsFieldEditor_BrightRed;
	public static String TerminalColorsFieldEditor_BrightWhite;
	public static String TerminalColorsFieldEditor_BrightYellow;
	public static String TerminalColorsFieldEditor_Cyan;
	public static String TerminalColorsFieldEditor_GeneralColors;
	public static String TerminalColorsFieldEditor_Green;
	public static String TerminalColorsFieldEditor_LoadPresets;
	public static String TerminalColorsFieldEditor_Magenta;
	public static String TerminalColorsFieldEditor_PaletteColors;
	public static String TerminalColorsFieldEditor_Presets;
	public static String TerminalColorsFieldEditor_Red;
	public static String TerminalColorsFieldEditor_SelectedText;
	public static String TerminalColorsFieldEditor_Selection;
	public static String TerminalColorsFieldEditor_TextColor;
	public static String TerminalColorsFieldEditor_White;
	public static String TerminalColorsFieldEditor_Yellow;
	public static String TerminalError;
	public static String SocketError;
	public static String IOError;
	public static String CannotConnectTo;
	public static String NotInitialized;

	//Preference Page
	public static String INVERT_COLORS;
	public static String BUFFERLINES;

}
