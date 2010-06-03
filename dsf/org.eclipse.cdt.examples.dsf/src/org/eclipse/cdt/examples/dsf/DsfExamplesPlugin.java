/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DsfExamplesPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.examples.dsf"; //$NON-NLS-1$

    public static final String IMG_LAYOUT_TOGGLE = "icons/layout.gif"; //$NON-NLS-1$
    public static final String IMG_ALARM = "icons/alarm.gif"; //$NON-NLS-1$
    public static final String IMG_ALARM_TRIGGERED = "icons/alarm_triggered.gif"; //$NON-NLS-1$
    public static final String IMG_TIMER = "icons/timer.gif"; //$NON-NLS-1$
    public static final String IMG_REMOVE = "icons/remove.gif"; //$NON-NLS-1$

	// The shared instance
	private static DsfExamplesPlugin fgPlugin;
	
    private static BundleContext fgBundleContext; 
    
	/**
	 * The constructor
	 */
	public DsfExamplesPlugin() {
		fgPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
        getImageRegistry().put(IMG_ALARM, imageDescriptorFromPlugin(PLUGIN_ID, IMG_ALARM));
        getImageRegistry().put(IMG_ALARM_TRIGGERED, imageDescriptorFromPlugin(PLUGIN_ID, IMG_ALARM_TRIGGERED));
        getImageRegistry().put(IMG_TIMER, imageDescriptorFromPlugin(PLUGIN_ID, IMG_TIMER));
        getImageRegistry().put(IMG_REMOVE, imageDescriptorFromPlugin(PLUGIN_ID, IMG_REMOVE));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		super.stop(context);
        fgPlugin = null;
        fgBundleContext = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfExamplesPlugin getDefault() {
		return fgPlugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

}
