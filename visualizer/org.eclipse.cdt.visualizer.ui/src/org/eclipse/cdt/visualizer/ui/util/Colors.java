/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


// ---------------------------------------------------------------------------
// Colors
// ---------------------------------------------------------------------------

/**
 * Standard color constants.
 * 
 * This is basically a set of cached color resources
 * for commonly-used colors.
 * These are drawn from an associated UIResourceManager.
 * The initialize() method should be called immediately
 * after you create the resource manager.
 */
public class Colors {

	// --- static members ---
	
	/** UI Resource manager colors are drawn from */
	protected static UIResourceManager s_resources = null;

	// Color objects for all the standard "int" colors in the SWT class:
	public static Color WHITE = null;
	public static Color BLACK = null;
	public static Color RED = null;
	public static Color DARK_RED = null;
	public static Color GREEN = null;
	public static Color DARK_GREEN = null;
	public static Color YELLOW = null;
	public static Color DARK_YELLOW = null;
	public static Color BLUE = null;
	public static Color DARK_BLUE = null;
	public static Color MAGENTA = null;
	public static Color DARK_MAGENTA = null;
	public static Color CYAN = null;
	public static Color DARK_CYAN = null;
	public static Color GRAY = null;
	public static Color DARK_GRAY = null;

	// Other "custom" Color objects:
	public static Color ORANGE = null;
	public static Color MEDIUM_GREEN = null;
	public static Color DARK_ORANGE = null;
	public static Color SEMI_DARK_YELLOW = null;
	public static Color DARK_MUTED_YELLOW = null;
	public static Color DARK_MUTED_GREEN = null;
	public static Color DARKER_RED = null;
	public static Color VERY_DARK_GRAY = null;
	public static Color WIDGET_UNEDITABLE_BACKGROUND = null;
	public static Color WIDGET_NORMAL_BACKGROUND = null;
	
	public static void initialize(UIResourceManager resources) {
		
		// set resource manager for future use
		s_resources = resources;
		
		// Color objects for all the standard "int" colors in the SWT class:
		WHITE             = getColor(SWT.COLOR_WHITE);
		BLACK             = getColor(SWT.COLOR_BLACK);
		RED               = getColor(SWT.COLOR_RED);
		DARK_RED          = getColor(SWT.COLOR_DARK_RED);
		GREEN             = getColor(SWT.COLOR_GREEN);
		DARK_GREEN        = getColor(SWT.COLOR_DARK_GREEN);
		YELLOW            = getColor(SWT.COLOR_YELLOW);
		DARK_YELLOW       = getColor(SWT.COLOR_DARK_YELLOW);
		BLUE              = getColor(SWT.COLOR_BLUE);
		DARK_BLUE         = getColor(SWT.COLOR_DARK_BLUE);
		MAGENTA           = getColor(SWT.COLOR_MAGENTA);
		DARK_MAGENTA      = getColor(SWT.COLOR_DARK_MAGENTA);
		CYAN              = getColor(SWT.COLOR_CYAN);
		DARK_CYAN         = getColor(SWT.COLOR_DARK_CYAN);
		GRAY              = getColor(SWT.COLOR_GRAY);
		DARK_GRAY         = getColor(SWT.COLOR_DARK_GRAY);

		// Other "custom" Color objects:
		ORANGE            = getColor(255, 150,  90);
		MEDIUM_GREEN      = getColor(  0, 192,   0);
		DARK_ORANGE       = getColor(215, 128,  81);
		SEMI_DARK_YELLOW  = getColor(192, 192,  0);
		DARK_MUTED_YELLOW = getColor( 90,  90,  50);
		DARK_MUTED_GREEN  = getColor( 18,  92,  18);
		DARKER_RED        = getColor(174,  25,  13);
		VERY_DARK_GRAY    = getColor( 60,  60,  60);
		
		WIDGET_NORMAL_BACKGROUND = getColor(SWT.COLOR_WHITE);
		WIDGET_UNEDITABLE_BACKGROUND = getColor(220,220,220);
	}
	
	// --- constructors/destructors ---

	/** Constructor
	 *  Private, since this is basically a bunch of constants.
	 */
	private Colors() {}

	/**
	 *  Gets color for a given SWT color constant.
	 */
	// final so this can be inlined where possible
	public static final Color getColor(int colorID) {
		return (s_resources == null) ? null : s_resources.getColor(colorID);
	}
	
	/**
	 *  Gets color for given RGB values (0-255).
	 */
	// final so this can be inlined where possible
	public static final Color getColor(int red, int green, int blue) {
		return (s_resources == null) ? null : s_resources.getColor(red, green, blue);
	}
}
