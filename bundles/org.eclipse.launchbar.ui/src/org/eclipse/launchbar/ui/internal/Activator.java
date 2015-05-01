/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.launchbar.ui"; //$NON-NLS-1$

	// Images
	public static final String IMG_BUTTON_BUILD = "build"; //$NON-NLS-1$
	public static final String IMG_BUTTON_LAUNCH = "launch"; //$NON-NLS-1$
	public static final String IMG_BUTTON_STOP = "stop"; //$NON-NLS-1$

	// Command ids
	public static final String CMD_BUILD = PLUGIN_ID + ".command.buildActive"; //$NON-NLS-1$
	public static final String CMD_LAUNCH = PLUGIN_ID + ".command.launchActive"; //$NON-NLS-1$
	public static final String CMD_STOP = PLUGIN_ID + ".command.stop"; //$NON-NLS-1$
	public static final String CMD_CONFIG = PLUGIN_ID + ".command.configureActiveLaunch"; //$NON-NLS-1$

	// Preference ids
	public static final String PREF_ENABLE_LAUNCHBAR = "enableLaunchBar"; //$NON-NLS-1$
	public static final String PREF_LAUNCH_HISTORY_SIZE = "launchHistorySize"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// The cache of the Launch Bar UI Manager Object
	private LaunchBarUIManager launchBarUIManager; 

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		ImageRegistry imageRegistry = getImageRegistry();
		imageRegistry.put(IMG_BUTTON_BUILD, imageDescriptorFromPlugin(PLUGIN_ID, "icons/build.png")); //$NON-NLS-1$
		imageRegistry.put(IMG_BUTTON_LAUNCH, imageDescriptorFromPlugin(PLUGIN_ID, "icons/launch.png")); //$NON-NLS-1$
		imageRegistry.put(IMG_BUTTON_STOP, imageDescriptorFromPlugin(PLUGIN_ID, "icons/stop.png")); //$NON-NLS-1$
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public LaunchBarUIManager getLaunchBarUIManager() {
		if (launchBarUIManager == null) {
			LaunchBarManager manager = (LaunchBarManager) getService(ILaunchBarManager.class);
			launchBarUIManager = new LaunchBarUIManager(manager);
		}
		return launchBarUIManager;
	}

	public Image getImage(String id) {
		Image im = getImageRegistry().get(id);
		if (im == null) {
			ImageDescriptor des = getImageDescriptor(id);
			if (des != null) {
				im = des.createImage();
				getImageRegistry().put(id, im);
			}
		}
		return im;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void runCommand(String commandId, String... params) {
		final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
				.getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		final Event trigger = new Event();
		final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, trigger);
		if (params.length == 0) {
			try {
				command.executeWithChecks(executionEvent);
			} catch (OperationCanceledException e) {
				// abort
			} catch (Exception e) {
				log(e);
			}
		} else {
			try {
				final Parameterization[] parameterizations = new Parameterization[params.length / 2];
				for (int i = 0; i < params.length; i += 2) {
					IParameter param = command.getParameter(params[i]);
					Parameterization parm = new Parameterization(param, params[i + 1]);
					parameterizations[i / 2] = parm;
				}
				ParameterizedCommand parmCommand = new ParameterizedCommand(command, parameterizations);
				handlerService.executeCommand(parmCommand, null);
			} catch (Exception e) {
				log(e);
			}
		}
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Exception e) {
		if (e instanceof CoreException)
			log(((CoreException) e).getStatus());
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
	}

	public static <T> T getService(Class<T> cls) {
		BundleContext context = getDefault().getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(cls);
		return ref != null ? context.getService(ref) : null;
	}
}
