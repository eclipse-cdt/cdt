package org.eclipse.cdt.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.resources.ICPlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.IMessageDialog;
import org.eclipse.cdt.core.resources.IPropertyStore;
import org.eclipse.cdt.core.ConsoleOutputStream;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.index.IndexModel;


public class CCorePlugin extends AbstractPlugin implements ICPlugin {
	
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
		
	public static Plugin getDefaultPlugin() {
		return fgCPlugin;
	}
	
	public static ICPlugin getDefault() {
		ICPlugin plugin;
		if ((plugin = (ICPlugin)fgCPlugin.getAdapter(ICPlugin.class)) != null) {
			return plugin;
		}
		return fgCPlugin;
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
	}
	
	public static void log(IStatus status) {
		((Plugin)getDefault()).getLog().log(status);
	}	
	
	public IPropertyStore getPropertyStore() {
		return getPreferenceStore();
	}
	
	// ------ CPlugin

	public IMessageDialog getMessageDialog() {
		return new IMessageDialog() {
			public void openError(String title, String msg) {
				System.err.println(title +": " +msg);
			}
		};
	}

	private IConsole fConsoleDocument;

	public CCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgCPlugin= this;
/*		
		fModel = new ACDebugModel() {
    		public Object createPresentation() {
    			return null;
    		}
    		
    		public String getIdentifier() {
    			return PLUGIN_ID;
    		}
    
			public IMarker createBreakpoint( final IResource resource, 
											final Map attributes,
											final String markerType ) throws CoreException {
				return null;
			}
		};
*/		
		fConsoleDocument= new IConsole() {
			public void clear() {
			}
			
			public ConsoleOutputStream getOutputStream() {
				return new ConsoleOutputStream();
			}
		};
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
	
	/**
	 * @see AbstractPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPropertyStore store) {
		super.initializeDefaultPreferences(store);
	}
	
	public IConsole getConsole() {
		return fConsoleDocument;
	}
	
	public CoreModel getCoreModel() {
		return CoreModel.getDefault();
	}

	public IndexModel getIndexModel() {
		return IndexModel.getDefault();
	}

/*	
	public ACDebugModel getDebugModel() {
		return fModel;
	}
*/
}
