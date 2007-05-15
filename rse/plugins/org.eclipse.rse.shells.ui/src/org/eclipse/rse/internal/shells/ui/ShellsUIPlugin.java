/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [180519] declaratively register rse.shells.ui. adapter factories
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 ********************************************************************************/

package org.eclipse.rse.internal.shells.ui;

import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class ShellsUIPlugin extends SystemBasePlugin {

	public static final String PLUGIN_ID ="org.eclipse.rse.shells.ui"; //$NON-NLS-1$
	public static final String HELPPREFIX = "org.eclipse.rse.shells.ui."; //$NON-NLS-1$
	
	// Icons
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String ICON_SUFFIX = "Icon";	 //$NON-NLS-1$
	public static final String ICON_EXT = ".gif";	 //$NON-NLS-1$

	// Special Model Object Icons
    public static final String ICON_OBJS_DIR = "full/obj16/";	 //$NON-NLS-1$
    
	public static final String ICON_SYSTEM_SHELL_ROOT = "systemshell"; // not used yet //$NON-NLS-1$
	public static final String ICON_SYSTEM_SHELL_ID = PREFIX + ICON_SYSTEM_SHELL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SHELL    = ICON_OBJS_DIR + ICON_SYSTEM_SHELL_ROOT + ICON_EXT;		

	public static final String ICON_SYSTEM_SHELLLIVE_ROOT = "systemshelllive"; // not used yet //$NON-NLS-1$
	public static final String ICON_SYSTEM_SHELLLIVE_ID = PREFIX + ICON_SYSTEM_SHELLLIVE_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_SHELLLIVE    = ICON_OBJS_DIR + ICON_SYSTEM_SHELLLIVE_ROOT + ICON_EXT;		

    // THING ICONS...
    public static final String ICON_MODEL_DIR = "full/obj16/";	 //$NON-NLS-1$

	public static final String ICON_SYSTEM_ENVVAR_ROOT = "systemenvvar";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ENVVAR      = ICON_MODEL_DIR + ICON_SYSTEM_ENVVAR_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ENVVAR_ID   = PREFIX+ICON_SYSTEM_ENVVAR+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_ENVVAR_LIBPATH_ROOT = "systemenvvarlibpath";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ENVVAR_LIBPATH      = ICON_MODEL_DIR + ICON_SYSTEM_ENVVAR_LIBPATH_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ENVVAR_LIBPATH_ID   = PREFIX+ICON_SYSTEM_ENVVAR_LIBPATH+ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_ENVVAR_PATH_ROOT = "systemenvvarpath";	 //$NON-NLS-1$
	public static final String ICON_SYSTEM_ENVVAR_PATH      = ICON_MODEL_DIR + ICON_SYSTEM_ENVVAR_PATH_ROOT+ICON_EXT;
	public static final String ICON_SYSTEM_ENVVAR_PATH_ID   = PREFIX+ICON_SYSTEM_ENVVAR_PATH+ICON_SUFFIX;
	

	// Action Icons    		
    public static final String ICON_ACTIONS_DIR = "full/elcl16/";	 //$NON-NLS-1$
    
	public static final String ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ROOT 	= "exportshelloutput"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_EXPORT_SHELL_OUTPUT      	= ICON_ACTIONS_DIR + ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ID  	= PREFIX + ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ROOT + ICON_SUFFIX;
	
	public static final String ICON_SYSTEM_EXPORT_SHELL_HISTORY_ROOT 	= "exportshellhistory"; //$NON-NLS-1$
	public static final String ICON_SYSTEM_EXPORT_SHELL_HISTORY      	= ICON_ACTIONS_DIR + ICON_SYSTEM_EXPORT_SHELL_HISTORY_ROOT + ICON_EXT;
	public static final String ICON_SYSTEM_EXPORT_SHELL_HISTORY_ID  	= PREFIX + ICON_SYSTEM_EXPORT_SHELL_HISTORY_ROOT + ICON_SUFFIX;

	public static final String ICON_SYSTEM_REMOVE_SHELL_ROOT = "removeshell";  //$NON-NLS-1$
	public static final String ICON_SYSTEM_REMOVE_SHELL_ID = PREFIX + ICON_SYSTEM_REMOVE_SHELL_ROOT + ICON_SUFFIX;
	public static final String ICON_SYSTEM_REMOVE_SHELL    = ICON_ACTIONS_DIR + ICON_SYSTEM_REMOVE_SHELL_ROOT + ICON_EXT;		

	//The shared instance.
	private static ShellsUIPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public ShellsUIPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception 
	{
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ShellsUIPlugin getDefault() {
		return plugin;
	}

    /* (non-Javadoc)
     * @see org.eclipse.rse.core.SystemBasePlugin#initializeImageRegistry()
     */
    protected void initializeImageRegistry()    
    {
    	//SystemElapsedTimer timer = new SystemElapsedTimer();
    	//timer.setStartTime();
    	
    	String path = getIconPath();

    	// Model Objects and Things
		putImageInRegistry(ICON_SYSTEM_SHELL_ID,
				   path+ICON_SYSTEM_SHELL);
		putImageInRegistry(ICON_SYSTEM_SHELLLIVE_ID,
				   path+ICON_SYSTEM_SHELLLIVE);
		putImageInRegistry(ICON_SYSTEM_ENVVAR_ID,
				   path+ICON_SYSTEM_ENVVAR);
		putImageInRegistry(ICON_SYSTEM_ENVVAR_LIBPATH_ID,
				   path+ICON_SYSTEM_ENVVAR_LIBPATH);
		putImageInRegistry(ICON_SYSTEM_ENVVAR_PATH_ID,
				   path+ICON_SYSTEM_ENVVAR_PATH);

    	// Actions...
		putImageInRegistry(ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ID,
	   			path+ICON_SYSTEM_EXPORT_SHELL_OUTPUT);
		putImageInRegistry(ICON_SYSTEM_EXPORT_SHELL_HISTORY_ID,
	   			path+ICON_SYSTEM_EXPORT_SHELL_HISTORY);
		putImageInRegistry(ICON_SYSTEM_REMOVE_SHELL_ID,
				   path+ICON_SYSTEM_REMOVE_SHELL);
		
        //timer.setEndTime();
        //System.out.println("Time to load images: "+timer);
    }

}