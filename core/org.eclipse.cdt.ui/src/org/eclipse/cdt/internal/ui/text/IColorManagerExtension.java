package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.graphics.RGB;


/**
 * For internal use only. Not API. <p>
 * A color manager extension is for extending
 * <code>IColorManager</code> instances with new functionality.
 */
public interface IColorManagerExtension {
	
	/**
	 * Remembers the given color specification under the given key.
	 *
	 * @param key the color key
	 * @param rgb the color specification
	 * @exception UnsupportedOperationException if there is already a
	 * 	color specification remembered under the given key
	 */
	void bindColor(String key, RGB rgb);
	
	
	/**
	 * Forgets the color specification remembered under the given key.
	 * @param key the color key
	 */
	void unbindColor(String key);
}