/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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
public class ColorManager
{
	private static ColorManager fgColorManager;

	private ColorManager()
	{
	}

	public static ColorManager getDefault()
	{
		if ( fgColorManager == null )
		{
			fgColorManager = new ColorManager();
		}
		return fgColorManager;
	}

	protected Map fColorTable = new HashMap( 10 );

	public Color getColor( RGB rgb )
	{
		Color color = (Color)fColorTable.get( rgb );
		if ( color == null )
		{
			color = new Color( Display.getCurrent(), rgb );
			fColorTable.put( rgb, color );
		}
		return color;
	}

	public void dispose()
	{
		Iterator e = fColorTable.values().iterator();
		while( e.hasNext() )
			((Color)e.next()).dispose();
	}
}
