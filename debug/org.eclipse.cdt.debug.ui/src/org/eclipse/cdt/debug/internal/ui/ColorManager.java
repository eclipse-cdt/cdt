/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * Color manager for C/C++ Debug UI.
 * 
 * @since Jul 23, 2002
 */
public class ColorManager {

	private static ColorManager gfColorManager;

	private ColorManager() {
	}

	public static ColorManager getDefault() {
		if ( gfColorManager == null ) {
			gfColorManager = new ColorManager();
		}
		return gfColorManager;
	}

	protected Map fColorTable = new HashMap( 10 );

	public Color getColor( RGB rgb ) {
		Color color = (Color)getColorTable().get(rgb);
		if ( color == null ) {
			color = new Color( Display.getCurrent(), rgb );
			getColorTable().put( rgb, color );
		}
		return color;
	}

	public void dispose() {
		Iterator e = getColorTable().values().iterator();
		while( e.hasNext() )
			 ((Color)e.next()).dispose();
	}

	private Map getColorTable() {
		return this.fColorTable;
	}
}
