/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager implements ISharedTextColors {
	public static final String MAKE_COMMENT_COLOR = "org.eclipse.cdt.make.ui.editor.comment"; //$NON-NLS-1$
	public static final String MAKE_KEYWORD_COLOR = "org.eclipse.cdt.make.ui.editor.keyword"; //$NON-NLS-1$
	public static final String MAKE_FUNCTION_COLOR = "org.eclipse.cdt.make.ui.editor.function"; //$NON-NLS-1$
	public static final String MAKE_MACRO_REF_COLOR = "org.eclipse.cdt.make.ui.editor.macro_ref"; //$NON-NLS-1$
	public static final String MAKE_MACRO_DEF_COLOR = "org.eclipse.cdt.make.ui.editor.macro_def"; //$NON-NLS-1$
	public static final String MAKE_DEFAULT_COLOR = "org.eclipse.cdt.make.ui.editor.default"; //$NON-NLS-1$
	public static final String MAKE_MATCHING_BRACKETS_COLOR = "org.eclipse.cdt.make.ui.editor.matching.brackets.color"; //$NON-NLS-1$

	public static final RGB MAKE_COMMENT_RGB = new RGB(63, 127, 95);
	public static final RGB MAKE_KEYWORD_RGB = new RGB(127, 0, 85);
	public static final RGB MAKE_FUNCTION_RGB = new RGB(208, 15, 63);
	public static final RGB MAKE_MACRO_DEF_RGB = new RGB(78, 118, 214);
	public static final RGB MAKE_MACRO_REF_RGB = new RGB(0, 0, 192);
	public static final RGB MAKE_DEFAULT_RGB = new RGB(0, 0, 0);
	public static final RGB MAKE_MATCHING_BRACKETS_RGB = new RGB(170, 170, 170);

	private static ColorManager fgColorManager;

	private ColorManager() {
	}

	public static ColorManager getDefault() {
		if (fgColorManager == null) {
			fgColorManager = new ColorManager();
		}
		return fgColorManager;
	}

	protected Map<RGB, Color> fColorTable = new HashMap<>(10);

	@Override
	public void dispose() {
		for (Color color : fColorTable.values()) {
			color.dispose();
		}
	}

	@Override
	public Color getColor(RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

}
