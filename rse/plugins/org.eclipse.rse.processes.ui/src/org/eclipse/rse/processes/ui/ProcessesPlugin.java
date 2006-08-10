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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.processes.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.processes.ui.view.RemoteProcessSubSystemConfigurationAdapterFactory;
import org.eclipse.rse.processes.ui.view.SystemViewProcessAdapterFactory;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class ProcessesPlugin extends SystemBasePlugin {
	//The shared instance.
	private static ProcessesPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private SystemViewProcessAdapterFactory svpaf;
	private static SystemMessageFile messageFile = null;    
    private static SystemMessageFile defaultMessageFile = null;    
	
	public static final String HELPPREFIX = "org.eclipse.rse.processes.ui.";

	
	/**
	 * The constructor.
	 */
	public ProcessesPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	   	messageFile = getMessageFile("systemmessages.xml");
	   	defaultMessageFile = getDefaultMessageFile("systemmessages.xml");

	    IAdapterManager manager = Platform.getAdapterManager();
		svpaf = new SystemViewProcessAdapterFactory();
		svpaf.registerWithManager(manager);

		RemoteProcessSubSystemConfigurationAdapterFactory rpssfaf = new RemoteProcessSubSystemConfigurationAdapterFactory();
		rpssfaf.registerWithManager(manager);
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
	public static ProcessesPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ProcessesPlugin.getDefault().getResourceBundle();
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
				resourceBundle = ResourceBundle.getBundle("org.eclipse.rse.processes.ui.SystemProcessesResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
	
	/**
	 * Retrieve a message from this plugin's message file
	 * @param msgId - the ID of the message to retrieve. This is the concatenation of the
	 *   message's component abbreviation, subcomponent abbreviation, and message ID as declared
	 *   in the message xml file.
	 */
    public static SystemMessage getPluginMessage(String msgId)
    {
    	SystemMessage msg = getMessage(messageFile, msgId);
    	if (msg == null)
    	{
    		msg = getMessage(defaultMessageFile, msgId);
    	}
    	return msg;
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
	 * Load a default message file for this plugin for cases where messages haven't been translated.
	 * @param messageFileName - the name of the message xml file. Will look for it in this plugin's install folder.
	 * @return a message file object containing the parsed contents of the message file, or null if not found.
	 */
	public SystemMessageFile getDefaultMessageFile(String messageFileName)
	{
	   return loadDefaultMessageFile(getBundle(), messageFileName);  	
	}
	
	public ImageDescriptor getImageDescriptorFromPath(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.rse.processes.ui", path);
	}
	
    /**
 	 *	Initialize the image registry by declaring all of the required
	 *	graphics.
	 */
    protected void initializeImageRegistry()    
    {
    	//String path = getIconPath();
 	    //putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWPROFILEWIZARD_ID,
		//				   path+ISystemIconConstants.ICON_SYSTEM_NEWPROFILEWIZARD);
		
    }
}