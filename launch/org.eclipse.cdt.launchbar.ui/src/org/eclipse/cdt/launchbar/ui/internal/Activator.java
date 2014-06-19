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
package org.eclipse.cdt.launchbar.ui.internal;

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
	public static final String PLUGIN_ID = "org.eclipse.cdt.launchbar.ui"; //$NON-NLS-1$

	// Images
	public static final String IMG_BUTTON_BUILD = "build";
	public static final String IMG_BUTTON_LAUNCH = "launch";
	public static final String IMG_BUTTON_STOP = "stop";

	// Command ids
	public static final String CMD_BUILD = "org.eclipse.cdt.launchbar.ui.command.buildActive";
	public static final String CMD_LAUNCH = "org.eclipse.cdt.launchbar.ui.command.launchActive";
	public static final String CMD_STOP = "org.eclipse.cdt.launchbar.ui.command.stop";
	public static final String CMD_CONFIG = "org.eclipse.cdt.launchbar.ui.command.configureActiveLaunch";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		ImageRegistry imageRegistry = getImageRegistry();
		imageRegistry.put(IMG_BUTTON_BUILD, imageDescriptorFromPlugin(PLUGIN_ID, "icons/build.png"));
		imageRegistry.put(IMG_BUTTON_LAUNCH, imageDescriptorFromPlugin(PLUGIN_ID, "icons/launch.png"));
		imageRegistry.put(IMG_BUTTON_STOP, imageDescriptorFromPlugin(PLUGIN_ID, "icons/stop.png"));
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

	public Image getImage(String id) {
		return getImageRegistry().get(id);
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

	public static void log(CoreException e) {
		plugin.getLog().log(e.getStatus());
	}

	public static void log(Exception e) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
	}

	public static <T> T getService(Class<T> cls) {
		BundleContext context = getDefault().getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(cls);
		return ref != null ? context.getService(ref) : null;
	}
}
