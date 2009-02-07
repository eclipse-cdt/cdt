/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * Takuya Miyamoto - [185925] Integrate Platform/Team Synchronization
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class RemoteImportExportPlugin extends SystemBasePlugin {

	public static final String PLUGIN_ID ="org.eclipse.rse.importexport"; //$NON-NLS-1$
	public static final String HELPPREFIX = "org.eclipse.rse.importexport."; //$NON-NLS-1$

	// Icons
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String ICON_BANNER_SUFFIX = "BannerIcon";	 //$NON-NLS-1$
	public static final String ICON_EXT = ".gif";	 //$NON-NLS-1$

	// WIZARD ICONS...
    public static final String ICON_WIZARD_DIR = "full/wizban/"; //$NON-NLS-1$
	public static final String ICON_EXPORTWIZARD_ROOT = "export_wiz";	 //$NON-NLS-1$
	public static final String ICON_EXPORTWIZARD    = ICON_WIZARD_DIR + ICON_EXPORTWIZARD_ROOT + ICON_EXT;
	public static final String ICON_EXPORTWIZARD_ID = PREFIX + ICON_EXPORTWIZARD_ROOT + ICON_BANNER_SUFFIX;
	public static final String ICON_IMPORTWIZARD_ROOT = "import_wiz";	 //$NON-NLS-1$
	public static final String ICON_IMPORTWIZARD = ICON_WIZARD_DIR + ICON_IMPORTWIZARD_ROOT + ICON_EXT;
	public static final String ICON_IMPORTWIZARD_ID = PREFIX + ICON_IMPORTWIZARD_ROOT + ICON_BANNER_SUFFIX;

	//The shared instance.
	private static RemoteImportExportPlugin plugin;

	/**
	 * The constructor.
	 */
	public RemoteImportExportPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
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
	public static RemoteImportExportPlugin getDefault() {
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
    	// Wizards...
		putImageInRegistry(ICON_EXPORTWIZARD_ID, path+ICON_EXPORTWIZARD);
		putImageInRegistry(ICON_IMPORTWIZARD_ID, path+ICON_IMPORTWIZARD);

        //timer.setEndTime();
        //System.out.println("Time to load images: "+timer);
    }

}
