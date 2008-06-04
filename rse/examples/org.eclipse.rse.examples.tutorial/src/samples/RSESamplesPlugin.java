/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 * Martin Oberhuber (Wind River) - [235626] Convert examples to MessageBundle format
 ********************************************************************************/

package samples;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;

import samples.subsystems.DeveloperSubSystemConfigurationAdapterFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class RSESamplesPlugin extends SystemBasePlugin  {
	//The shared instance.
	private static RSESamplesPlugin plugin;
	//Resource bundle.
	private static SystemMessageFile 	messageFile = null;

	/**
	 * The constructor.
	 */
	public RSESamplesPlugin() {
		super();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.SystemBasePlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		messageFile = getMessageFile("rseSamplesMessages.xml"); //$NON-NLS-1$

		IAdapterManager manager = Platform.getAdapterManager();
		samples.model.DeveloperAdapterFactory factory = new samples.model.DeveloperAdapterFactory();
		manager.registerAdapters(factory, samples.model.TeamResource.class);
		manager.registerAdapters(factory, samples.model.DeveloperResource.class);

	    DeveloperSubSystemConfigurationAdapterFactory sscaf = new DeveloperSubSystemConfigurationAdapterFactory();
	    sscaf.registerWithManager(manager);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.SystemBasePlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static RSESamplesPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 * @return the singleton Workspace from Eclipse Resources plugin
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Initialize the image registry by declaring all of the required graphics.
	 */
	protected void initializeImageRegistry()
	{
		String path = getIconPath();
		putImageInRegistry("ICON_ID_TEAM", path + "team.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_DEVELOPER", path + "developer.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_TEAMFILTER", path + "teamFilter.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_DEVELOPERFILTER", path + "developerFilter.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Load a message file for this plugin.
	 * @param messageFileName - the name of the message xml file. Will look for it in this plugin's install folder.
	 * @return a message file object containing the parsed contents of the message file, or null if not found.
	 */
    public SystemMessageFile getMessageFile(String messageFileName)
    {
       return loadMessageFile(getBundle(), messageFileName);
    }

	/**
	 * Return our message file.
	 *
	 * @return the RSE message file
	 */
	public static SystemMessageFile getPluginMessageFile()
	{
		return messageFile;
	}

	/**
	 * Retrieve a message from this plugin's message file,
	 * or <code>null</code> if the message cannot be found.
	 * @see SystemMessageFile#getMessage(String)
	 *
	 * @param msgId message id
	 * @return the message object referenced by the given id
	 */
	public static SystemMessage getPluginMessage(String msgId)
	{
		return getMessage(messageFile, msgId);
	}
}
