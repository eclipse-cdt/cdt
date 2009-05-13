/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
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
 * Kushal Munir (IBM) - moved to internal package
 * Martin Oberhuber (Wind River) - [181917] EFS Improvements: Avoid unclosed Streams,
 *    - Fix early startup issues by deferring FileStore evaluation and classloading,
 *    - Improve performance by RSEFileStore instance factory and caching IRemoteFile.
 *    - Also remove unnecessary class RSEFileCache and obsolete branding files.
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * David McKnight      (IBM) - [241316] [efs] Cannot restore editors for RSE/EFS-backed resources
 ********************************************************************************/

package org.eclipse.rse.internal.efs;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	//The shared instance.
	private static Activator plugin;

	public static final String PLUGIN_ID = "org.eclipse.rse.efs"; //$NON-NLS-1$


	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}


	public void start(BundleContext context) throws Exception {
		super.start(context);
		final RemoteEditorManager mgr = RemoteEditorManager.getDefault();
		ResourcesPlugin.getWorkspace().addSaveParticipant(this, mgr);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(mgr, IResourceChangeEvent.POST_CHANGE);

		Job job = new Job("Add Listener"){					 //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {

				while (!PlatformUI.isWorkbenchRunning()){
					try {
						//Checks in the loop are fast enough so we can poll often
						Thread.sleep(100);
					}
					catch (InterruptedException e){}
				}
				IWorkbench wb = PlatformUI.getWorkbench();
				wb.addWorkbenchListener(mgr);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

	}

	public void stop(BundleContext context) throws Exception {
		try {
			ResourcesPlugin.getWorkspace().removeSaveParticipant(this);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(RemoteEditorManager.getDefault());
		} finally {
			super.stop(context);
		}
		plugin = null;
	}
	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static IStatus errorStatus(Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e);
	}

}