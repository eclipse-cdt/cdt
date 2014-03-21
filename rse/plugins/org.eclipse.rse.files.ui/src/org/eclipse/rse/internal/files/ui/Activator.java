/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [205820] create the temp files project (if not there) when files.ui is loaded
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Martin Oberhuber (Wind River) - [228353] Asynchronously initialize the remote edit project
 * David McKnight   (IBM)        - [245260] Different user's connections on a single host are mapped to the same temp files cache
 * David McKnight   (IBM)        - [430896] On shutdown, RSE should call SystemRemoteEditManager.refreshRemoteEditProject() in order to clear cache files
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.files.ui.resources.SystemUniversalTempFileListener;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin
{

	//The shared instance.
	private static Activator plugin;

	private static SystemUniversalTempFileListener _tempFileListener;

	public final static String PLUGIN_ID = "org.eclipse.rse.files.ui"; //$NON-NLS-1$
	public static final String HELPPREFIX = "org.eclipse.rse.files.ui."; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	private class InitRemoteEditJob extends Job {
		public InitRemoteEditJob() {
			super("InitRemoteEditJob"); //$NON-NLS-1$
		}

		protected IStatus run(IProgressMonitor monitor) {
			// create the temp files project if it doesn't exist
			// fix for bug 205820
			SystemRemoteEditManager.getInstance().getRemoteEditProject();

			// refresh the remote edit project at plugin startup, to ensure
			// it's never closed
			SystemRemoteEditManager.getInstance().refreshRemoteEditProject();

			// universal temp file listener
			_tempFileListener = SystemUniversalTempFileListener.getListener();
			// add listener for temp files
			int eventMask = IResourceChangeEvent.POST_CHANGE;
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			ws.addResourceChangeListener(_tempFileListener, eventMask);

			return Status.OK_STATUS;
		}

	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

    	// DKM - workaround for issue in  175295
    	// I had tried SystemFilePreferenceInitializer but it was not being started by the platform because
    	// the preference store is rse.ui. In order to fix that, we'd have to migrate the
    	// preferences for files to the files.ui preference store.
    	// Instead calling this directly at startup.
    	initializeDefaultRSEPreferences();

		//Bug 228353: Initialize remote edit project in a Job
    	//The Job must run AFTER initializeDefaultRSEPreferences(), because that one
    	//needs some classes loaded, and the class loader could fall into a deadlock
    	//when the deferred Job also wants to load some classes but cannot continue
    	//because the start() method is not yet finished
		InitRemoteEditJob job = new InitRemoteEditJob();
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();

	}

	/**
	 * Initializes preferences.
	 */
	public void initializeDefaultRSEPreferences()
	{
		//FIXME This should really be migrated into a Preferences Initializer Extension
		//in order to avoid unnecessary plugin activation
		//IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();


		// as per bug 245260, using scoped store for preference initialization
		IPreferenceStore store = new ScopedPreferenceStore(new DefaultScope(), RSEUIPlugin.getDefault().getBundle().getSymbolicName());

		// system cache preferences
		if (store.isDefault(ISystemFilePreferencesConstants.LIMIT_CACHE)){
			store.setDefault(ISystemFilePreferencesConstants.LIMIT_CACHE, ISystemFilePreferencesConstants.DEFAULT_LIMIT_CACHE);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.MAX_CACHE_SIZE)){
			store.setDefault(ISystemFilePreferencesConstants.MAX_CACHE_SIZE, ISystemFilePreferencesConstants.DEFAULT_MAX_CACHE_SIZE);
		}


		// universal preferences
		if (store.isDefault(ISystemFilePreferencesConstants.FILETRANSFERMODEDEFAULT)){
			store.setDefault(ISystemFilePreferencesConstants.FILETRANSFERMODEDEFAULT, ISystemFilePreferencesConstants.DEFAULT_FILETRANSFERMODE);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.SHOWHIDDEN)){
			store.setDefault(ISystemFilePreferencesConstants.SHOWHIDDEN, false);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.PRESERVETIMESTAMPS)){
			store.setDefault(ISystemFilePreferencesConstants.PRESERVETIMESTAMPS, ISystemFilePreferencesConstants.DEFAULT_PRESERVETIMESTAMPS);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.SHARECACHEDFILES)){
			store.setDefault(ISystemFilePreferencesConstants.SHARECACHEDFILES, ISystemFilePreferencesConstants.DEFAULT_SHARECACHEDFILES);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.DOSUPERTRANSFER)){
			store.setDefault(ISystemFilePreferencesConstants.DOSUPERTRANSFER, ISystemFilePreferencesConstants.DEFAULT_DOSUPERTRANSFER);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE)){
			store.setDefault(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE, ISystemFilePreferencesConstants.DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE)){
			store.setDefault(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE, ISystemFilePreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE);
		}

		if (store.isDefault(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE)){
			store.setDefault(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE, ISystemFilePreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE);
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);
		if (_tempFileListener != null) {
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			ws.removeResourceChangeListener(_tempFileListener);
			_tempFileListener = null;
			SystemRemoteEditManager.getInstance().refreshRemoteEditProject();
		}
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.rse.files.ui", path); //$NON-NLS-1$
	}

}
