/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [180519][api] declaratively register adapter factories
 * Martin Oberhuber (wind River) - [203105] Decouple recursive plugin activation of UI adapters
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	//The shared instance.
	private static Activator plugin;

	public static final String PLUGIN_ID = "org.eclipse.rse.subsystems.files.core"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	class RSEDefaultEncodingProvider extends SystemEncodingUtil.DefaultEncodingProvider{
		public String getLocalDefaultEncoding() {
			return ResourcesPlugin.getEncoding();
		}
		
		public boolean isXML(String path){
			boolean result = super.isXML(path);
			if (!result){
				// now check the extension point mappings
				RemoteFileUtility.getSystemFileTransferModeRegistry().isXML(path);
			}
						
			return result;
		}
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		Job setupEncodingProvider = new Job(SystemFileResources.RESID_JOB_SETUP_ENCODING_PROVIDER){ //$NON-NLS-1$
			
			public IStatus run(IProgressMonitor monitor){
				// set the default encoding provider
				SystemEncodingUtil encodingUtil = SystemEncodingUtil.getInstance();
				encodingUtil.setDefaultEncodingProvider(new RSEDefaultEncodingProvider());								
				return Status.OK_STATUS;
			}
		};
		setupEncodingProvider.setSystem(true);
		setupEncodingProvider.schedule();
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
	public static Activator getDefault() {
		return plugin;
	}

}
