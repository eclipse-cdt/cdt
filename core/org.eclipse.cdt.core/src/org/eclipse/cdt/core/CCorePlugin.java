package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;


public class CCorePlugin extends Plugin {
	
	public static final String PLUGIN_ID= "org.eclipse.cdt.core";
	public static final String BUILDER_ID= PLUGIN_ID + ".cbuilder";
		
	private static CCorePlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;

	// -------- static methods --------
	
	static {
		try {
			fgResourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.internal.CCorePluginResources");
		} catch (MissingResourceException x) {
			fgResourceBundle= null;
		}
	}

	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		} catch (NullPointerException e) {
			return "#" + key + "#";
		}
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}	
	
	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}
	
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}
	
	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
		
	public static CCorePlugin getDefault() {
		return fgCPlugin;
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
	}
	
	public static void log(IStatus status) {
		((Plugin)getDefault()).getLog().log(status);
	}	
		
	// ------ CPlugin

	public CCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgCPlugin= this;
	}
		
	/**
	 * @see Plugin#shutdown
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
	}		
	
	/**
	 * @see Plugin#startup
	 */
	public void startup() throws CoreException {
		super.startup();

		// Fired up the model.
		getCoreModel();
		// Fired up the indexer. It should delay itself for 10 seconds
		getIndexModel();
	}
	
	public IConsole getConsole() throws CoreException {
		IConsole consoleDocument = null;

		IExtensionPoint extension = getDescriptor().getExtensionPoint("CBuildConsole");
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				consoleDocument = (IConsole)configElements[0].createExecutableExtension("class");
			}
		}		
		if ( consoleDocument == null ) {
			return new IConsole() {
				public void clear() {
				}
			
				public ConsoleOutputStream getOutputStream() {
					return new ConsoleOutputStream();
				}
			};
		}
		return consoleDocument;
	}
	
	public CoreModel getCoreModel() {
		return CoreModel.getDefault();
	}

	public IndexModel getIndexModel() {
		return IndexModel.getDefault();
	}	
}
