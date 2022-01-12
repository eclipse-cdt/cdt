/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Anton Leherbauer (Wind River) - [294468] Fix scroller and text line rendering
 * Martin Oberhuber (Wind River) - [265352][api] Allow setting fonts programmatically
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;

import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.terminal.model.TerminalColor;

/**
 *
 */
public interface ILinelRenderer {
	int getCellWidth();

	int getCellHeight();

	void drawLine(ITextCanvasModel model, GC gc, int line, int x, int y, int colFirst, int colLast);

	/**
	 * Update for a font change from the global JFace Registry.
	 * @deprecated Use {@link #updateFont(String)}
	 */
	@Deprecated
	void onFontChange();

	/**
	 * Set a new font
	 * @param fontName Jface name of the new font
	 * @since 3.2
	 */
	void updateFont(String fontName);

	void updateColors(Map<TerminalColor, RGB> map);

	void setInvertedColors(boolean invert);

	boolean isInvertedColors();

	/**
	 * @deprecated use {@link #getDefaultBackgroundColor(Device)}
	 */
	@Deprecated
	Color getDefaultBackgroundColor();

	Color getDefaultBackgroundColor(Device device);

}
