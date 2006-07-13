/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 ********************************************************************************/

package samples;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.osgi.framework.BundleContext;

import samples.subsystems.DeveloperSubSystemConfigurationAdapterFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class RSESamplesPlugin extends SystemBasePlugin  {
	//The shared instance.
	private static RSESamplesPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private static SystemMessageFile 	messageFile = null;
	
	/**
	 * The constructor.
	 */
	public RSESamplesPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
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

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static RSESamplesPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= RSESamplesPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("samples.rseSamplesResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	//protected void initializeDefaultPreferences(IPreferenceStore store) 
	//{
	//	super.initializeDefaultPreferences(store);
	//	//RSESamplesPreferencePage.initDefaults(store);
	//}
	
	/**
	 * Initialize the image registry by declaring all of the required graphics.
	 */
	protected void initializeImageRegistry()
	{
		String path = getIconPath();
		putImageInRegistry("ICON_ID_TEAM",      path+"/team.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_DEVELOPER", path+"/developer.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_TEAMFILTER",      path+"/teamFilter.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		putImageInRegistry("ICON_ID_DEVELOPERFILTER", path+"/developerFilter.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		// TO RETRIEVE AN ICON, CALL GETIMAGE OR GETIMAGEDESCRIPTOR WITH ITS XXX_ID ID
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
	 * Return our message file
	 */
	public static SystemMessageFile getPluginMessageFile()
	{
		return messageFile;
	}  

	/**
	 * Retrieve a message from this plugin's message file
	 */
	public static SystemMessage getPluginMessage(String msgId)
	{
		return getMessage(messageFile, msgId);
	}
}
