package org.eclipse.cdt.internal.ui.text.util;
 
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.text.IColorManagerExtension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Java color manager.
 */
public class CColorManager implements IColorManager, IColorManagerExtension {
	
	protected Map fKeyTable= new HashMap(10);
	protected Map fDisplayTable= new HashMap(2);
	
	
	public CColorManager() {
	}
	
	protected void dispose(Display display) {		
		Map colorTable= (Map) fDisplayTable.get(display);
		if (colorTable != null) {
			Iterator e= colorTable.values().iterator();
			while (e.hasNext())
				((Color) e.next()).dispose();
		}
	}
	
	/*
	 * @see IColorManager#getColor(RGB)
	 */
	public Color getColor(RGB rgb) {
		
		if (rgb == null)
			return null;
		
		final Display display= Display.getCurrent();
		Map colorTable= (Map) fDisplayTable.get(display);
		if (colorTable == null) {
			colorTable= new HashMap(10);
			fDisplayTable.put(display, colorTable);
			display.disposeExec(new Runnable() {
				public void run() {
					dispose(display);
				}
			});
		}
		
		Color color= (Color) colorTable.get(rgb);
		if (color == null) {
			color= new Color(Display.getCurrent(), rgb);
			colorTable.put(rgb, color);
		}
		
		return color;
	}
	
	/*
	 * @see IColorManager#dispose
	 */
	public void dispose() {
		dispose(Display.getCurrent());
	}
	
	/*
	 * @see IColorManager#getColor(String)
	 */
	public Color getColor(String key) {
		
		if (key == null)
			return null;
			
		RGB rgb= (RGB) fKeyTable.get(key);
		return getColor(rgb);
	}
	
	/*
	 * @see IColorManagerExtension#bindColor(String, RGB)
	 */
	public void bindColor(String key, RGB rgb) {
		Object value= fKeyTable.get(key);
		if (value != null)
			throw new UnsupportedOperationException();
		
		fKeyTable.put(key, rgb);
	}

	/*
	 * @see IColorManagerExtension#unbindColor(String)
	 */
	public void unbindColor(String key) {
		fKeyTable.remove(key);
	}
}


