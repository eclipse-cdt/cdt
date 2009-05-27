/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.examples.dsf.pda.launch.PDALaunch;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
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
    
    public static String PLUGIN_ID = "org.eclipse.cdt.examples.dsf.pda.ui "; 
    
	//The shared instance.
	private static PDAUIPlugin plugin;
	
	private static BundleContext fContext;

	private final static String ICONS_PATH = "icons/full/";//$NON-NLS-1$
	private final static String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons //$NON-NLS-1$
    
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
    private Map<RGB, Color> fColors = new HashMap<RGB, Color>();
    	
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
	@Override
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
	@Override
	public void stop(BundleContext context) throws Exception {
		disposeAdapterSets();
		super.stop(context);
		plugin = null;
		fContext = null;
        for (Map.Entry<RGB, Color> entry : fColors.entrySet()) {
            entry.getValue().dispose();
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
	    
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		declareImage(IMG_OBJ_PDA, PATH_OBJECT + "pda.gif");
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
        URL url = BundleUtility.find("org.eclipse.cdt.examples.dsf.pda.ui", path);
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
        Color color = fColors.get(rgb);
        if (color == null) {
            color= new Color(Display.getCurrent(), rgb);
            fColors.put(rgb, color);
        }
        return color;
    }
    
	/**
	 * Dispose adapter sets for all launches.
	 */
	private void disposeAdapterSets() {
        for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
            if (launch instanceof PDALaunch) {
                PDAAdapterFactory.disposeAdapterSet(launch);
            }
        }
	}

 }
