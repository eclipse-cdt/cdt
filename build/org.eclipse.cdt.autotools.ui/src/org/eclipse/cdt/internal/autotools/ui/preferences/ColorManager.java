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
package org.eclipse.cdt.internal.autotools.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager implements ISharedTextColors {

	public static final String MAKE_COMMENT_COLOR ="org.eclipse.cdt.autotools.ui.automake.editor.comment"; //$NON-NLS-1$
	public static final String MAKE_KEYWORD_COLOR = "org.eclipse.cdt.autotools.ui.automake.editor.keyword"; //$NON-NLS-1$
	public static final String MAKE_FUNCTION_COLOR = "org.eclipse.cdt.autotools.ui.automake.editor.function"; //$NON-NLS-1$
	public static final String MAKE_MACRO_REF_COLOR = "org.eclipse.cdt.autotools.ui.automake.editor.macro_ref"; //$NON-NLS-1$
	public static final String MAKE_MACRO_DEF_COLOR = "org.eclipse.cdt.autotools.ui.automake.editor.macro_def"; //$NON-NLS-1$
	public static final String MAKE_DEFAULT_COLOR = "org.eclipse.cdt.autotools.ui.automake.editor.default"; //$NON-NLS-1$

	public static final String AUTOCONF_COMMENT_COLOR ="org.eclipse.cdt.autotools.ui.autoconf.editor.comment"; //$NON-NLS-1$
	public static final String AUTOCONF_KEYWORD_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.keyword"; //$NON-NLS-1$
	public static final String AUTOCONF_VAR_REF_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.var_ref"; //$NON-NLS-1$
	public static final String AUTOCONF_VAR_SET_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.var_set"; //$NON-NLS-1$
	public static final String AUTOCONF_ACMACRO_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.acmacro"; //$NON-NLS-1$
	public static final String AUTOCONF_AMMACRO_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.ammacro"; //$NON-NLS-1$
	public static final String AUTOCONF_CODESEQ_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.codeseq"; //$NON-NLS-1$
	public static final String AUTOCONF_DEFAULT_COLOR = "org.eclipse.cdt.autotools.ui.autoconf.editor.default"; //$NON-NLS-1$

	public static final RGB MAKE_COMMENT_RGB = new RGB(128, 0, 0);
	public static final RGB MAKE_KEYWORD_RGB = new RGB(128, 255, 0);
	public static final RGB MAKE_FUNCTION_RGB = new RGB(128, 0, 128);
	public static final RGB MAKE_MACRO_DEF_RGB = new RGB(0, 0, 128);
	public static final RGB MAKE_MACRO_REF_RGB = new RGB(0, 128, 0);
	public static final RGB MAKE_DEFAULT_RGB = new RGB(0, 0, 0);

	public static final RGB AUTOCONF_COMMENT_RGB = new RGB(63, 95, 191);
	public static final RGB AUTOCONF_KEYWORD_RGB = new RGB(127, 0, 85);
	public static final RGB AUTOCONF_VAR_REF_RGB = new RGB(128, 0, 0);
	public static final RGB AUTOCONF_VAR_SET_RGB = new RGB(255, 101, 52);
	public static final RGB AUTOCONF_ACMACRO_RGB = new RGB(0, 0, 128);
	public static final RGB AUTOCONF_AMMACRO_RGB = new RGB(0, 128, 0);
	public static final RGB AUTOCONF_CODESEQ_RGB = new RGB(0, 100, 0);
	public static final RGB AUTOCONF_DEFAULT_RGB = new RGB(0, 0, 0);

	private static ColorManager fgColorManager;
	
	private ColorManager() {
	}
	
	public static ColorManager getDefault() {
		if (fgColorManager == null) {
			fgColorManager= new ColorManager();
		}
		return fgColorManager;
	}

	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

	/**
	 * @see IMakefileColorManager#dispose()
	 */
	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext())
			 ((Color) e.next()).dispose();
	}

	/**
	 * @see IMakefileColorManager#getColor(RGB)
	 */
	public Color getColor(RGB rgb) {
		Color color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

}
