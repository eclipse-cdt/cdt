/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Moved from org.eclipse.rse.services
 * David McKnight   (IBM)        - [209704] [api] Ability to override default encoding conversion needed.
 *******************************************************************************/

package org.eclipse.rse.internal.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.CodePageConverterManager;
import org.eclipse.rse.services.files.IFileServiceCodePageConverter;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	//The shared instance.
	private static Activator plugin;
	public static final String PLUGIN_ID="org.eclipse.rse.services"; //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerArchiveHandlers();
		registerCodePageConverters();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Initializes the Archive Handler Manager, by registering archive \
	 * file types with their handlers.
	 * @author mjberger
	 */
	protected void registerArchiveHandlers()
	{
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		// Get configured extenders
		IConfigurationElement[] systemTypeExtensions = registry.getConfigurationElementsFor("org.eclipse.rse.services", "archivehandlers"); //$NON-NLS-1$ //$NON-NLS-2$
		     	
		for (int i = 0; i < systemTypeExtensions.length; i++) {
			String ext = systemTypeExtensions[i].getAttribute("fileNameExtension"); //$NON-NLS-1$
			if (ext.startsWith(".")) ext = ext.substring(1); //$NON-NLS-1$
			String handlerType = systemTypeExtensions[i].getAttribute("class"); //$NON-NLS-1$
			try
			{	
				// get the name space of the declaring extension
			    String nameSpace = systemTypeExtensions[i].getDeclaringExtension().getNamespaceIdentifier();
				
				// use the name space to get the bundle
			    Bundle bundle = Platform.getBundle(nameSpace);
			    
			    // if the bundle has not been uninstalled, then load the handler referred to in the
			    // extension, and load it using the bundle
			    // then register the handler
			    if (bundle.getState() != Bundle.UNINSTALLED) {
			        Class handler = bundle.loadClass(handlerType);
			        ArchiveHandlerManager.getInstance().setRegisteredHandler(ext, handler);
			    }
			}
			catch (ClassNotFoundException e)
			{
				logException(e);
			}
		}
	}



	
	private void registerCodePageConverters()
	{
		// retrieve all extension points
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint("org.eclipse.rse.services", "codePageConverters"); //$NON-NLS-1$ //$NON-NLS-2$
		if (ep != null){
			IExtension[] extensions = ep.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configElements = extension.getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					IConfigurationElement element = configElements[j];
					if (element.getName().equalsIgnoreCase("codePageConverter")) {								 //$NON-NLS-1$
						try {
							Object codePageConverter = element.createExecutableExtension("class"); //$NON-NLS-1$
							if (codePageConverter!=null && codePageConverter instanceof IFileServiceCodePageConverter){
								// only save extension point which implement the correct interface
								CodePageConverterManager.registerCodePageConverter((IFileServiceCodePageConverter)codePageConverter);							
							}
						} 
						catch (CoreException e) {
							//shouldn't get here....
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Logs an throwable to the log for this plugin.
	 * @param t the Throwable to be logged.
	 */
	public void logException(Throwable t) {
		ILog log = getLog();
		String id = getBundle().getSymbolicName();
		IStatus status = new Status(IStatus.ERROR, id, 0, "Unexpected exception", t); //$NON-NLS-1$
		log.log(status);
	}

	//<tracing code>----------------------------------------------------

	private static Boolean fTracingOn = null;
	public static boolean isTracingOn() {
		if (fTracingOn==null) {
			String id = plugin.getBundle().getSymbolicName();
			String val = Platform.getDebugOption(id + "/debug"); //$NON-NLS-1$
			if ("true".equals(val)) { //$NON-NLS-1$
				fTracingOn = Boolean.TRUE;
			} else {
				fTracingOn = Boolean.FALSE;
			}
		}
		return fTracingOn.booleanValue();
	}
	public static String getTimestamp() {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$
			return formatter.format(new Date());
		} catch (Exception e) {
			// If there were problems writing out the date, ignore and
			// continue since that shouldn't stop us from logging the rest
			// of the information
		}
		return Long.toString(System.currentTimeMillis());
	}
	public static void trace(String msg) {
		if (isTracingOn()) {
			String fullMsg = getTimestamp() + " | " + Thread.currentThread().getName() + " | " + msg; //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println(fullMsg);
			System.out.flush();
		}
	}

	//</tracing code>---------------------------------------------------
}
