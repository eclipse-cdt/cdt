/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import java.net.URL;

import org.eclipse.cdt.visualizer.core.ResourceManager;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;


// ---------------------------------------------------------------------------
// UIResourceManager
// ---------------------------------------------------------------------------

/**
 * Plugin resource manager.
 * Provides one-stop-shopping for UI plugin resources
 * (strings, images, fonts, colors).
 * This class should be instanced in the UI plugin's "start()" method,
 * and disposed in the "stop()" method.
 */
public class UIResourceManager extends ResourceManager
{
	// --- members ---
	
	/** UI Plugin */
	protected AbstractUIPlugin m_UIplugin = null;
	
	/** Parent resource manager, if any */
	protected UIResourceManager m_parentUIManager = null;
	
	/** Image resource manager */
	protected ImageRegistry m_images = null;
	
	/** Font resource manager */
	protected FontRegistry m_fonts = null;
	
	/** Color resource manager */
	protected ColorRegistry m_colors = null;
	
	
	// --- constructors/destructors ---
	
	/** Constructor
	 *  Note: the plugin ID is assumed to also be the parent package name
	 *  of the plugin, for example "com.tilera.ide.core"; it's assumed that string
	 *  resources are found in the plugin directory in "messages.properties".
	 */
	public UIResourceManager(AbstractUIPlugin plugin) {
		super(plugin);
		m_UIplugin = plugin;
		getImageRegistry(); // creates registry object
		getFontRegistry();  // creates registry object
		getColorRegistry(); // creates registry object
	}
	
	/** Dispose method */
	public void dispose() {
		disposeImageRegistry();
		disposeFontRegistry();
		disposeColorRegistry();
	}	

	
	// --- parent manager management ---
	
	/** Sets parent resource manager, if any */
	public void setParentManager(ResourceManager parentManager) {
		super.setParentManager(parentManager);
		if (parentManager instanceof UIResourceManager) {
			m_parentUIManager = (UIResourceManager) parentManager;
		}
		else {
			m_parentUIManager = null;
		}
	}

	/** Gets parent UI resource manager, if any */
	public UIResourceManager getParentManager() {
		return m_parentUIManager;
	}

	
	// --- image resource management ---
	
	/** Creates/returns image registry */
	protected ImageRegistry getImageRegistry() {
		if (m_images == null) {
			// use the plugin's image registry
			m_images = m_UIplugin.getImageRegistry();
		}
		return m_images;
	}
	
	/** Disposes image registry */
	protected void disposeImageRegistry() {
		if (m_images != null) {
			// we're using the plugin's image registry, so it will dispose of it
			//m_images.dispose();
			m_images = null;
		}
	}

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     */
    public ImageDescriptor getImageDescriptor(String path) {
    	ImageDescriptor result = null;
		if (path == null) return result;
    	
    	// get image registry, look up path
    	ImageRegistry images = getImageRegistry();
    	result = images.getDescriptor(path);
    	
    	// if we don't find it, see if it exists as a file in this plugin
    	if (result == null) {
    		URL url = m_UIplugin.getBundle().getEntry(path);
    		if (url != null) {
        		// if so, we'll add an entry for it to this resource manager
    			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
	    		images.put(path, desc);
	    		result = images.getDescriptor(path);
    		}
    		else {
    			// if not, see if we can find it in the parent manager
    			// NOTE: this may be in a different plugin, so the path will be
    			// resolved relative to that plugin's base directory
    			UIResourceManager parent = getParentManager();
    			if (parent != null) {
    				result = parent.getImageDescriptor(path);
    			}
    		}
    	}
    	return result;
    }
    
    /**
     * Gets specified image from plugin directory.
     * Caches images so subsequent requests will be faster.
     */
    public Image getImage(String path) {
        Image result = null;
		if (path == null) return result;
        
    	// get image registry, look up path
    	ImageRegistry images = getImageRegistry();
    	result = images.get(path);
    	
    	// if we don't find it, see if it exists as a file in this plugin
    	if (result == null) {
    		URL url = m_UIplugin.getBundle().getEntry(path);
    		if (url != null) {
        		// if so, we'll add an entry for it to this resource manager
    			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
	    		images.put(path, desc);
	    		result = images.get(path);
    		}
    		else {
    			// if not, see if we can find it in the parent manager
    			// NOTE: this may be in a different plugin, so the path will be
    			// resolved relative to that plugin's base directory
    			UIResourceManager parent = getParentManager();
    			if (parent != null) {
    				result = parent.getImage(path);
    			}
    		}
    	}
    	return result;
    }
    
    
    // --- font registry ---
    
    /** Creates/returns font registry */
    protected FontRegistry getFontRegistry() {
    	if (m_fonts == null) {
    		Display display = getDisplay();
    		m_fonts = new FontRegistry(display);
    	}
    	return m_fonts;
    }

	/** Disposes font registry */
	protected void disposeFontRegistry() {
		if (m_fonts != null) {
			// doesn't appear to be a way to flush the registry cache
			// so let finalizer handle it
			m_fonts = null;
		}
	}

    /** Sets cached font for specified ID */
    public void setFont(String fontID, String fontName, int height, int style) {
    	setCachedFont(fontID, fontName, height, style);
    }

    /** Gets cached font for specified ID */
    public Font getFont(String fontID) {
    	return getCachedFont(fontID);
    }

    /** Gets/creates font with specified properties */
    public Font getFont(String fontName, int height) {
    	return getCachedFont(fontName, height, SWT.NORMAL);
    }

    /** Gets/creates font with specified properties */
    public Font getFont(String fontName, int height, int style) {
    	return getCachedFont(fontName, height, style);
    }

    /** Sets cached font for specified ID */
    protected void setCachedFont(String fontID, String fontName, int height, int style) {
    	FontData[] fontData = new FontData[1];
    	fontData[0] = new FontData(fontName, height, style);
    	FontRegistry fonts = getFontRegistry();
    	fonts.put(fontID, fontData);
    }

    /** Gets cached font, if any, for specified ID */
    protected Font getCachedFont(String fontID) {
    	Font result = null;
    	
    	// get font registry, look up font ID
    	FontRegistry fonts = getFontRegistry();
    	if (fonts.hasValueFor(fontID)) {
    		result = fonts.get(fontID);
    	}
    	else {
    		// if we don't find it, see if parent manager has it
    		UIResourceManager parent = getParentManager();
    		if (parent != null) {
    			result = parent.getCachedFont(fontID);
    		}
    	}
    	return result;
    }

    /** Gets/creates font with specified properties */
    protected Font getCachedFont(String fontName, int height, int style) {
    	Font result = null;
    	String fontID = fontName + "," + height + "," + style;
    	
    	// look for the cached font (this checks the parent manager too)
    	result = getCachedFont(fontID);
    	
    	// if we didn't find it, add an entry to this resource manager
    	if (result == null) {
    		setCachedFont(fontID, fontName, height, style);
    		result = getCachedFont(fontID);
    	}
    	return result;
    }

    
    // --- color registry ---

    /** Creates/returns color registry */
    protected ColorRegistry getColorRegistry() {
    	if (m_colors == null) {
    		Display display = getDisplay();
    		m_colors = new ColorRegistry(display);
    	}
    	return m_colors;
    }

	/** Disposes color registry */
	protected void disposeColorRegistry() {
		if (m_colors != null) {
			// doesn't appear to be a way to flush the registry cache
			// so let finalizer handle it
			m_colors = null;
		}
	}

    /** Gets cached color with specified SWT color ID */
    public Color getColor(int colorID) {
    	// NOTE: we don't cache these colors, we just look them up every time
    	return getDisplay().getSystemColor(colorID);
    }

    /** Gets cached color with specified ID */
    public Color getColor(String colorID) {
    	return getCachedColor(colorID);
    }

    /** Gets/creates color with specified properties */
    public Color getColor(int red, int green, int blue) {
    	return getCachedColor(red, green, blue);
    }
    
    /** Sets cached color for specified ID */
    protected void setCachedColor(String colorID, int red, int green, int blue) {
    	RGB rgb = new RGB(red, green, blue);
    	ColorRegistry colors = getColorRegistry();
    	colors.put(colorID, rgb);
    }

    /** Gets cached color with specified ID */
    protected Color getCachedColor(String colorID) {
    	Color result = null;
    	ColorRegistry colors = getColorRegistry();
    	if (colors.hasValueFor(colorID)) {
    		result = colors.get(colorID);
    	}
    	else {
    		// if we don't find it, see if parent manager has it
    		UIResourceManager parent = getParentManager();
    		if (parent != null) {
    			result = parent.getCachedColor(colorID);
    		}
    	}
    	return result;
    }

    /** Gets/creates color with specified properties */
    protected Color getCachedColor(int red, int green, int blue) {
    	Color result = null;
    	String colorID = "Color[R=" + red + ",G=" + green + ",B=" + blue + "]";
    	
    	// look for the cached color (this checks the parent manager too)
    	result = getCachedColor(colorID);
    	
    	// if we don't find it, create an entry in this resource manager
    	if (result == null) {
    		setCachedColor(colorID, red, green, blue);
    		result = getCachedColor(colorID);
    	}
    	return result;
    }
    
    
    // --- utilities ---
    
	/**
	 * Returns the current SWT display.
	 * 
	 * The method first checks whether the caller already has
	 * an associated display. If so, this display is returned.
	 * Otherwise the method returns the default display.
	 * 
	 * This allows GUIUtils to work in contexts like tests
	 * where the SWT display has not already been defined.
	 * 
	 * (Credit: Borrowed from DebugUIPlugin.)
	 */
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;
	}
}
