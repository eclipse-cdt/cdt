/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.model.Style;
import org.eclipse.tm.terminal.model.StyleColor;

public class StyleMap {
	String fFontName=JFaceResources.TEXT_FONT;
	Map fColorMap=new HashMap();
	Map fFontMap=new HashMap();
	private Point fCharSize;
	private Style fDefaultStyle;
	StyleMap() {
		Display display=Display.getCurrent();
		fColorMap.put(StyleColor.getStyleColor("white"), new Color(display,255,255,255)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("black"), new Color(display,0,0,0)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("red"), new Color(display,255,128,128)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("green"), new Color(display,128,255,128)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("blue"), new Color(display,128,128,255)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("yellow"), new Color(display,255,255,0)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("cyan"), new Color(display,0,255,255)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("magenta"), new Color(display,255,255,0)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("gray"), new Color(display,128,128,128)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("WHITE"), new Color(display,255,255,255)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("BLACK"), new Color(display,0,0,0)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("RED"), new Color(display,255,128,128)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("GREEN"), new Color(display,128,255,128)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("BLUE"), new Color(display,128,128,255)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("YELLOW"), new Color(display,255,255,0)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("CYAN"), new Color(display,0,255,255)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("MAGENTA"), new Color(display,255,255,0)); //$NON-NLS-1$
		fColorMap.put(StyleColor.getStyleColor("GRAY"), new Color(display,128,128,128)); //$NON-NLS-1$
		fDefaultStyle=Style.getStyle(StyleColor.getStyleColor("black"),StyleColor.getStyleColor("white")); //$NON-NLS-1$ //$NON-NLS-2$
		GC gc = new GC (display);
		gc.setFont(getFont());
		fCharSize = gc.textExtent ("W"); //$NON-NLS-1$
		gc.dispose ();

	}
	public Color getColor(StyleColor colorName) {
		return (Color) fColorMap.get(colorName);
	}
	public Color getForegrondColor(Style style) {
		style = defaultIfNull(style);
		if(style.isReverse())
			return getColor(style.getBackground());
		else
			return getColor(style.getForground());
	}
	private Style defaultIfNull(Style style) {
		if(style==null)
			style=fDefaultStyle;
		return style;
	}
	public Color getBackgroundColor(Style style) {
		style = defaultIfNull(style);
		if(style.isReverse())
			return getColor(style.getForground());
		else
			return getColor(style.getBackground());
	}
//	static Font getBoldFont(Font font) {
//		FontData fontDatas[] = font.getFontData();
//		FontData data = fontDatas[0];
//		return new Font(Display.getCurrent(), data.getName(), data.getHeight(), data.getStyle()|SWT.BOLD);
//	}

	public Font getFont(Style style) {
		style = defaultIfNull(style);
		if(style.isBold()) {
			return  JFaceResources.getFontRegistry().getBold(fFontName);
		} else if(style.isUnderline()) {
			return  JFaceResources.getFontRegistry().getItalic(fFontName);
			
		}
		return  JFaceResources.getFontRegistry().get(fFontName);
	}
	
	public Font getFont() {
		return  JFaceResources.getFontRegistry().get(fFontName);
		
	}
	public int getFontWidth() {
		return fCharSize.x;
	}
	public int getFontHeight() {
		return fCharSize.y;
	}
}
