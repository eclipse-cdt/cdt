/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PDAUIPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static PDAUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	private static BundleContext fContext;

	private final static String ICONS_PATH = "icons/full/";//$NON-NLS-1$
	private final static String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons //$NON-NLS-1$
    private final static String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; //Enabled local toolbar icons //$NON-NLS-1$
    private final static String PATH_DLOCALTOOL = ICONS_PATH + "dlcl16/"; //Disabled local toolbar icons //$NON-NLS-1$
    
    /**
     *  Toolbar action to pop data stack
     */
    public final static String IMG_ELCL_POP = "IMG_ELCL_POP";
    public final static String IMG_DLCL_POP = "IMG_DLCL_POP";
    
    /**
     * Toolbar action to push onto data stack
     */
    public final static String IMG_ELCL_PUSH = "IMG_ELCL_PUSH";
    public final static String IMG_DLCL_PUSH = "IMG_DLCL_PUSH";
    
    /**
     * PDA program image
     */
    public final static String IMG_OBJ_PDA = "IMB_OBJ_PDA";
    
    /**
     * Keyword color
     */
    public final static RGB KEYWORD = new RGB(0,0,255);
    public final static RGB LABEL = new RGB(128, 128, 0);
    
    /**
     * Managed colors
     */
    private Map fColors = new HashMap();
    	
	/**
	 * The constructor.
	 */
	public PDAUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
        fContext = context;
		super.start(context);
//		Toggles single threaded adapter example
//		IAdapterManager adapterManager = Platform.getAdapterManager();
//		IAdapterFactory factory = new AdapterFactory();
//		adapterManager.registerAdapters(factory, PDADebugTarget.class);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
		fContext = null;
        Iterator colors = fColors.entrySet().iterator();
        while (colors.hasNext()) {
            Map.Entry entry = (Entry) colors.next();
            ((Color)entry.getValue()).dispose();
        }
	}

	/**
	 * Returns the shared instance.
	 */
	public static PDAUIPlugin getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
	    return fContext;
	}
	    

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PDAUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.debug.examples.ui.pda.DebugUIPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		declareImage(IMG_OBJ_PDA, PATH_OBJECT + "pda.gif");
	    declareImage(IMG_ELCL_POP, PATH_ELOCALTOOL + "pop.gif");
	    declareImage(IMG_DLCL_POP, PATH_DLOCALTOOL + "pop.gif");
	    declareImage(IMG_ELCL_PUSH, PATH_ELOCALTOOL + "push.gif");
	    declareImage(IMG_DLCL_PUSH, PATH_DLOCALTOOL + "push.gif");
	}
	
    /**
     * Declares a workbench image given the path of the image file (relative to
     * the workbench plug-in). This is a helper method that creates the image
     * descriptor and passes it to the main <code>declareImage</code> method.
     * 
     * @param symbolicName the symbolic name of the image
     * @param path the path of the image file relative to the base of the workbench
     * plug-ins install directory
     * <code>false</code> if this is not a shared image
     */
    private void declareImage(String key, String path) {
        URL url = BundleUtility.find("org.eclipse.debug.examples.ui", path);
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getImageRegistry().put(key, desc);
    }
    
    /**
     * Returns the color described by the given RGB.
     * 
     * @param rgb
     * @return color
     */
    public Color getColor(RGB rgb) {
        Color color = (Color) fColors.get(rgb);
        if (color == null) {
            color= new Color(Display.getCurrent(), rgb);
            fColors.put(rgb, color);
        }
        return color;
    }
    
 }
