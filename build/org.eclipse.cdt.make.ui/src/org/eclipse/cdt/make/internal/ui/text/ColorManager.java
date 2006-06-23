/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager implements ISharedTextColors {

	public static final String MAKE_COMMENT_COLOR ="org.eclipse.cdt.make.ui.editor.comment"; //$NON-NLS-1$
	public static final String MAKE_KEYWORD_COLOR = "org.eclipse.cdt.make.ui.editor.keyword"; //$NON-NLS-1$
	public static final String MAKE_FUNCTION_COLOR = "org.eclipse.cdt.make.ui.editor.function"; //$NON-NLS-1$
	public static final String MAKE_MACRO_REF_COLOR = "org.eclipse.cdt.make.ui.editor.macro_ref"; //$NON-NLS-1$
	public static final String MAKE_MACRO_DEF_COLOR = "org.eclipse.cdt.make.ui.editor.macro_def"; //$NON-NLS-1$
	public static final String MAKE_DEFAULT_COLOR = "org.eclipse.cdt.make.ui.editor.default"; //$NON-NLS-1$

	public static final RGB MAKE_COMMENT_RGB = new RGB(128, 0, 0);
	public static final RGB MAKE_KEYWORD_RGB = new RGB(128, 255, 0);
	public static final RGB MAKE_FUNCTION_RGB = new RGB(128, 0, 128);
	public static final RGB MAKE_MACRO_DEF_RGB = new RGB(0, 0, 128);
	public static final RGB MAKE_MACRO_REF_RGB = new RGB(0, 128, 0);
	public static final RGB MAKE_DEFAULT_RGB = new RGB(0, 0, 0);

	private static ColorManager fgColorManager;
	
	private ColorManager() {
	}
	
	public static ColorManager getDefault() {
		if (fgColorManager == null) {
			fgColorManager= new ColorManager();
		}
		return fgColorManager;
	}

	protected Map fColorTable = new HashMap(10);

	/**
	 * @see IMakefileColorManager#dispose()
	 */
	public void dispose() {
		Iterator e = fColorTable.values().iterator();
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
