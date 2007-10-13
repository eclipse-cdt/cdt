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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.model.Style;
import org.eclipse.tm.terminal.model.StyleColor;

public class StyleMap {
	private static final String BLACK = "black"; //$NON-NLS-1$
	private static final String WHITE = "white"; //$NON-NLS-1$
	private static final String GRAY = "gray"; //$NON-NLS-1$
	private static final String MAGENTA = "magenta"; //$NON-NLS-1$
	private static final String CYAN = "cyan"; //$NON-NLS-1$
	private static final String YELLOW = "yellow"; //$NON-NLS-1$
	private static final String BLUE = "blue"; //$NON-NLS-1$
	private static final String GREEN = "green"; //$NON-NLS-1$
	private static final String RED = "red"; //$NON-NLS-1$
	
	private static final String PREFIX = "org.eclipse.tm.internal."; //$NON-NLS-1$
	// TODO propagate the name of the fonf in the FontRegistry
	String fFontName="terminal.views.view.font.definition"; //$NON-NLS-1$
	Map fColorMap=new HashMap();
	Map fFontMap=new HashMap();
	private Point fCharSize;
	private Style fDefaultStyle;
	private boolean fInvertColors;
	StyleMap() {
		addColor(WHITE, 255,255,255);
		addColor(BLACK, 0,0,0); 
		addColor(RED, 255,128,128); 
		addColor(GREEN, 128,255,128); 
		addColor(BLUE, 128,128,255); 
		addColor(YELLOW, 255,255,0); 
		addColor(CYAN, 0,255,255); 
		addColor(MAGENTA, 255,255,0); 
		addColor(GRAY, 128,128,128); 
		updateFont();
	}
	private void addColor(String name, int r, int g, int b) {
		String colorName=PREFIX+name;
		Color color=JFaceResources.getColorRegistry().get(colorName);
		if(color==null) {
			JFaceResources.getColorRegistry().put(colorName, new RGB(r,g,b));
			color=JFaceResources.getColorRegistry().get(colorName);
		}
		fColorMap.put(StyleColor.getStyleColor(name), color);
		fColorMap.put(StyleColor.getStyleColor(name.toUpperCase()), color);
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
	public void setInvertedColors(boolean invert) {
		if(invert==fInvertColors)
			return;
		fInvertColors=invert;
		swapColors(WHITE,BLACK); 
		fDefaultStyle=Style.getStyle(StyleColor.getStyleColor(BLACK),StyleColor.getStyleColor(WHITE)); 
	}
	void swapColors(String n1, String n2) {
		swapColors2(n1, n2);
		swapColors2(n1.toUpperCase(), n2.toUpperCase());
	}

	void swapColors2(String n1, String n2) {
		Color c1=getColor(StyleColor.getStyleColor(n1));
		Color c2=getColor(StyleColor.getStyleColor(n2));
		fColorMap.put(StyleColor.getStyleColor(n1), c2);
		fColorMap.put(StyleColor.getStyleColor(n2), c1);
		
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
	public void updateFont() {
		Display display=Display.getCurrent();
		GC gc = new GC (display);
		gc.setFont(getFont());
		fCharSize = gc.textExtent ("W"); //$NON-NLS-1$
		gc.dispose ();
	}
}
