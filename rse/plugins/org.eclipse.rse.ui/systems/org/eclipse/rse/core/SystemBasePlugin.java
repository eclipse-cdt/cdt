/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.logging.LoggerFactory;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.ui.messages.SystemUIMessageFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * A base plugin class offering common operations.
 */
public abstract class SystemBasePlugin extends AbstractUIPlugin 
{

	/**
	 * Default folder for icons, relative to this plugin's install folder: "icons".
	 */
	private static final String ICON_PATH = "icons";

	// static variables
    private static SystemBasePlugin baseInst = null;

    /**
     * Logger object for logging messages for servicing purposes.
     */
	protected static Logger log = null;		
    
	// instance variables
    private Hashtable imageDescriptorRegistry = null;	
    private boolean headless;
    private boolean headlessSet;

    /**
     * Returns the singleton object representing the base plugin.
     * @return the singleton object.
     */
    public static SystemBasePlugin getBaseDefault() {
	    return baseInst;
    }
    
	/**
	 * Returns the active workbench shell.
	 * @return the active workbench shell.
	 */
	public static Shell getActiveWorkbenchShell() {
	    
	    IWorkbenchWindow window = getActiveWorkbenchWindow();
	    
		if (window != null) {
			return window.getShell();
		}
		
		return null;
	}

	/**
	 * Returns the active workbench window.
	 * @return the active workbench window. 
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
	    
		IWorkbench wb = null;
		
		try {
		    wb = getBaseDefault().getWorkbench();
		}
		catch (Exception exc) {
		    // in headless mode
		    wb = null;
		}
		
		// if we are not in headless mode
		if (wb != null) {
		    
		    // if in user interface thread, return the workbench active window
			if (Display.getCurrent() != null) {
				return wb.getActiveWorkbenchWindow();
			}
			// otherwise, get a list of all the windows, and simply return the first one
			// KM: why do we need this??
			else {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
				
				if (windows != null && windows.length > 0) {
					return windows[0];
				}
			}
			
			return null;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the workspace root.
	 * @return the workspace root.
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
	    return getWorkspace().getRoot();
	}

	/**
	 * Returns the workspace.
	 * @return the workspace.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * @return the prefix of the path for icons, i.e. "icons/".
	 */
	public static String getIconPath() {
		return ICON_PATH;
	}

	/**
	 * Retrieve image in any plugin's directory tree, given its file name.
	 * The file name should be qualified relative to this plugin's bundle. Eg "icons/myicon.gif"
	 */
	public static ImageDescriptor getPluginImage(Bundle bundle, String fileName)
	{
	   URL path = bundle.getEntry("/" + fileName);
	   ImageDescriptor descriptor = ImageDescriptor.createFromURL(path);
	   return descriptor;
	}

	// ----------------------------------------
	// TRANSLATABLE RESOURCE-RELATED METHODS...
	// ----------------------------------------
	
	/**
	 * Sets the resource bundle. Called by child class in their
	 *  constructor, say, to load in their resource bundle. Handles
	 *  rare case when not found by telling user, then dying.
	 *  Note: This is NOT to be used for plugin.properties since Eclipse handles that file differently.
	 * @param descriptor for the plugin where the file is
	 * @param name of .properties file, without the '.properties' specified
	 * @return ResourceBundle if loaded successfully, null if not.
	 */
//	public static final ResourceBundle loadResourceBundle(Bundle bundle, String fileName)
//	{
//	    ResourceBundle rb = null;
//	    boolean ok = false;
//	    try
//		{
//			URL url = resolveBundleNameNL(bundle, fileName+".properties");
//			logDebugMessage("SystemBasePlugin", "Trying this now: " + url.toString());
//			InputStream in = url.openStream();
//			rb = new PropertyResourceBundle(in);
//			in.close();
//			ok = true;
//			
//	    } 
//	    catch (Exception exc)
//	    {
//		    logError("SystemBasePlugin - try for resource bundle " + fileName + " not successful!",exc);
//	    }
//	    
//	    if (!ok)
//	    {
//	      Shell s = getActiveWorkbenchShell();
//	      
//	      if (s == null) {
//	      	Display d = Display.getCurrent();
//	      	
//	      	if (d != null) {
//	      		s = d.getActiveShell();
//	      	}
//	      	else {
//	      		d = Display.getDefault();
//	      		
//	      		if (d != null) {
//	      			s = d.getActiveShell();
//	      		}
//	      	}
//	      }
//	      
//	      if (s != null) {
//	      	org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(s);
//	      	mb.setText("Unexcepted Error");
//	      	mb.setMessage("Unable to load resource file " + fileName + ".properties");
//	      	mb.open();
//	      }
//	    }
//	    
//	    return rb;
//	}

	/**
	 * Returns the plugin.properties resource bundle associated with the specified plugin descriptor
	 * 
	 * @param descriptor for the plugin where the file is
	 * @return ResourceBundle if loaded successfully, null if not.
	 */
//	public static final ResourceBundle loadPluginResourceBundle(Bundle bundle)
//	{
//	     ResourceBundle rb = null;
//	     try
//	    {
//	         rb = Platform.getResourceBundle(bundle);
//	     } 
//	     catch (Exception exc)
//	     {
//	         logInfo("try for resource bundle plugin.properties not successful!");
//	     }
//	
//	     return rb;
//	}

	/**
	   * Sets the default resource bundle for handling cases where strings aren't translated. Called by child class in their
	   *  constructor, say, to load in their resource bundle. Handles
	   *  rare case when not found by telling user, then dying.
	   *  Note: This is NOT to be used for plugin.properties since Eclipse handles that file differently.
	   * @param descriptor for the plugin where the file is
	   * @param name of .properties file, without the '.properties' specified
	   * @return ResourceBundle if loaded successfully, null if not.
	   */
//	  public static final ResourceBundle loadDefaultResourceBundle(Bundle bundle,
//															   String fileName)
//	  {
//		  ResourceBundle rb = null;
//		  boolean ok = false;
//		  try
//		  {
//			  String name = fileName+".properties";
//			  URL url = bundle.getEntry("/"+name);
//			  logDebugMessage("SystemBasePlugin", "Trying this now: " + url.toString());
//			  InputStream in = url.openStream();
//			  rb = new PropertyResourceBundle(in);
//			  in.close();
//			  ok = true;
//		  } 
//		  catch (Exception exc)
//		  {
//			  logError("SystemBasePlugin - try for resource bundle " + fileName + " not successful!",exc);
//		  }
//	    
//		  if (!ok)
//		  {
//		  	
//		  	Shell s = getActiveWorkbenchShell();
//		      
//		    if (s == null) {
//		      Display d = Display.getCurrent();
//		      	
//		      if (d != null) {
//		      	s = d.getActiveShell();
//		      }
//		      else {
//		      	d = Display.getDefault();
//		      		
//		      	if (d != null) {
//		      		s = d.getActiveShell();
//		      	}
//		      }
//		    }
//		  	
//		    if (s != null) {
//		    	org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(s);
//		    	mb.setText("Unexcepted Error");
//		    	mb.setMessage("Unable to load resource file " + fileName + ".properties");
//		    	mb.open();
//		    }
//		  }
//		  
//		  return rb;
//	  }

	/**
	 * Get a string from a given resource bundle.
	 * If not found, stack trace info is placed in the
	 *  plugin's log file to help pinpoint the offending code.
	 */
//	public static String getString(ResourceBundle resourceBundle, String key)
//	{
//	    try
//	    {
//		   return resourceBundle.getString(key);
//	    }
//	    catch (MissingResourceException exc)
//	    {
//		   SystemBasePlugin.logError("Missing resource: " + key, exc);		   
//	    }
//	    return null;
//	}

	/**
	 * Get a string from a given resource bundle, with an english string to 
	 *  use a default if the given key is not found. 
	 * <p>
	 * If not found, stack trace info is placed in the
	 *  plugin's log file to help pinpoint the offending code.
	 */
//	public static String getString(ResourceBundle resourceBundle, String key, String defaultString)
//	{
//		String s = defaultString;
//	    try
//	    {
//		   s = resourceBundle.getString(key);
//	    }
//	    catch (MissingResourceException exc)
//	    {	    	
//		   SystemBasePlugin.logError("Missing resource: " + key, exc);		   
//	    }
//	    return s;
//	}

	// ------------------    
	// MESSAGE METHODS...
	// ------------------
	
	/**
	 * Resolves the bundle relative name to its URL inside a bundle if the resource
	 * named by that name exists. Returns null if the resources does not exist.
	 * Looks for the resource in NL directories as well.
	 * @param bundle The bundle in which to look for the resource
	 * @param name The name of the resource
	 * @return The resource URL or null.
	 */
	public static final URL resolveBundleNameNL(Bundle bundle, String name) {
		URL result = null;
		Stack candidates = new Stack();
		Locale locale = Locale.getDefault();
		String language = locale.getLanguage();
		String country = locale.getCountry();
		candidates.push("/" + name);
		if (language.length() > 0) {
			candidates.push("/" + language + "/" + name);
			if (country.length() > 0) {
				candidates.push("/" + language + "/" + country + "/" + name);
			}
		}
		while (!candidates.isEmpty() && result == null) {
			String candidate = (String) candidates.pop();
			result = bundle.getEntry(candidate);
		}
		return result;
	}
	
	/**
	 * Parse the given message file into memory, into a SystemMessageFile
	 * object.
	 * 
	 * @param descriptor -
	 *            the descriptor for this plugin
	 * @param fileName -
	 *            unqualified name of the .xml message file, inluding the .xml
	 *            extension.
	 * @return SystemMessageFile (null if unable to load the file)
	 */
	public static final SystemMessageFile loadMessageFile(Bundle bundle,
			String fileName) {
		SystemMessageFile mf = null;
		boolean ok = false;
		try {
			URL url = resolveBundleNameNL(bundle, fileName);
			if (url != null) {
				InputStream messageFileStream = url.openStream();
				mf = SystemUIMessageFile.getMessageFile(fileName, messageFileStream);
				messageFileStream.close();
				ok = true;
			}
		} catch (Throwable t) {
			logError("Error loading message file "
					+ fileName
					+ " in "
					+ bundle.getHeaders().get(
							org.osgi.framework.Constants.BUNDLE_NAME), t);
			ok = false; // DY
		}
		if (!ok) {
			MessageBox mb = new MessageBox(getActiveWorkbenchShell());
			mb.setText("Unexpected Error");
			mb.setMessage("Unable to load message file "
					+ fileName
					+ " in "
					+ bundle.getHeaders().get(
							org.osgi.framework.Constants.BUNDLE_NAME));
			mb.open();
		}
		return mf;
	}

	/**
	 * Parse the given message file into memory, into a SystemMessageFile
	 * object.
	 * 
	 * @param descriptor -
	 *            the descriptor for this plugin
	 * @param fileName -
	 *            unqualified name of the .xml message file, inluding the .xml
	 *            extension.
	 * @return SystemMessageFile (null if unable to load the file)
	 */
	public static final SystemMessageFile loadDefaultMessageFile(Bundle bundle,
			String fileName) {
		SystemMessageFile mf = null;
		boolean ok = false;
		try {
			URL url = bundle.getEntry("/"+fileName);
			if (url != null) {
				InputStream messageFileStream = url.openStream();
				mf = SystemUIMessageFile.getMessageFile(fileName, messageFileStream);
				messageFileStream.close();
				ok = true;
			}
		} catch (Throwable t) {
			logError("Error loading message file "
					+ fileName
					+ " in "
					+ bundle.getHeaders().get(
							org.osgi.framework.Constants.BUNDLE_NAME), t);
			ok = false; // DY
		}

		if (!ok) {
			Shell s = getActiveWorkbenchShell();
			if (s == null) {
				Display d = Display.getCurrent();
				if (d != null) {
					s = d.getActiveShell();
				} else {
					d = Display.getDefault();
					if (d != null) {
						s = d.getActiveShell();
					}
				}
			}
			if (s != null) {
				MessageBox mb = new MessageBox(s);
				mb.setText("Unexpected Error");
				mb.setMessage("Unable to load message file "
						+ fileName
						+ " in "
						+ bundle.getHeaders().get(
								org.osgi.framework.Constants.BUNDLE_NAME));
				mb.open();
			}
		}

		return mf;
	}

	/**
	 * Retrieve a message from a message file.
	 * 
	 * @param msgFile -
	 *            the system message file containing the message.
	 * @param msgId -
	 *            the ID of the message to retrieve. This is the concatenation
	 *            of the message's component abbreviation, subcomponent
	 *            abbreviation, and message ID as declared in the message xml
	 *            file.
	 */
	public static SystemMessage getMessage(SystemMessageFile msgFile, String msgId)
	{
	   SystemMessage msg = null;
	   if ( msgFile != null )
	   	 msg = msgFile.getMessage(msgId);
	   else
	     logWarning("No message file set.");
	
	   if ( msg == null )
	     logWarning("Unable to find message ID: " + msgId);
	   return msg;  	
	}

	/**
	 * Scan this plugin's message file for duplicates. This just calls the {@link org.eclipse.rse.ui.messages.SystemMessageFile#scanForDuplicates()}
	 * method on the SystemMessageFile object.
	 * @param msgFile - the message file to scan
	 * @return true if duplicates found. The duplicates are written to standard out and the system core log file.
	 */
	public static boolean scanForDuplicateMessages(SystemMessageFile msgFile)
	{
		return msgFile.scanForDuplicates();
	}

	/**
	 * Generate HTML from this plugin's message file. This is handy for documentation purposes.
	 * This just calls the {@link org.eclipse.rse.ui.messages.SystemMessageFile#printHTML(String)}
	 * method on the SystemMessageFile object.
	 * @param msgFile - the message file to print
	 * @return true if all went well, false if it failed for some reason.
	 */
	public static boolean printMessages(SystemMessageFile msgFile, String fullyQualifiedTargetFile)
	{
		return msgFile.printHTML(fullyQualifiedTargetFile);
	}

	// -----------------    
	// LOGGER METHODS...
	// -----------------
	      
	/**
	 * Helper method for logging information to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin.
	 * 
	 * @param message - System message to be written to the log file
	 */               	        
	public static void logMessage(SystemMessage message)
	{
		logMessage(message, null);
	}

	/**
	 * Helper method for logging information to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin.
	 * 
	 * @param message - System message to be written to the log file
	 * @param ex - Exception to log.  If not applicable, this can be null.
	 */               	        
	public static void logMessage(SystemMessage message, Throwable ex)
	{
		char type = message.getIndicator();
		switch (type)
		{
			case SystemMessage.ERROR:
				log.logError(message.toString(), ex);	
				break;
			case SystemMessage.WARNING: 
				log.logWarning(message.toString(), ex);
				break;
			case SystemMessage.INFORMATION:
			case SystemMessage.COMPLETION:
				log.logInfo(message.toString(), ex);
				break;
			case SystemMessage.INQUIRY:
			case SystemMessage.UNEXPECTED:
			default:
				log.logInfo(message.toString(), ex);
				break;							
		}                       	       
	}

	/**
	 * Helper method for logging information to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin.
	 * <p>
	 * Because this is an information message, it will only actually be logged if the
	 * user has enabled logging of information messages via the Logging preferences page
	 * within the Remote Systems preference pages tree.
	 * 
	 * @param message - Message to be written to the log file
	 */
	public static void logInfo(String message) 
	{
		log.logInfo(message);
	}

	// -----------------    
	// LOGGER METHODS...
	// -----------------
	      
	/**
	 * Helper method for logging warnings to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin.
	 * <p>
	 * Because this is a warning message, it will only actually be logged if the
	 * user has enabled logging of warning messages via the Logging preferences page
	 * within the Remote Systems preference pages tree.
	 * 
	 * @param message - Message to be written to the log file
	 * Because these messages are only used for servicing purposes, the message typically is not translated.
	 */
	public static void logWarning(String message) 
	{
		log.logWarning(message);
	}

	/**
	 * Helper method for logging errors (but not exceptions) to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin. 
	 * <p>
	 * Because this is an error message, it is always logged, no matter what the preferences settings for
	 * the logger.
	 * 
	 * @param message - Message to be written to the log file
	 * Because these messages are only used for servicing purposes, the message typically is not translated.
	 */
	public static void logError(String message) 
	{
		log.logError(message, null);
	}

	/**
	 * Helper method for logging errors (exceptions) to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin. 
	 * <p>
	 * Because this is an error message, it is always logged, no matter what the preferences settings for
	 * the logger.
	 * 
	 * @param message - Message to be written to the log file. 
	 * Because these messages are only used for servicing purposes, the message typically is not translated.
	 * 
	 * @param exception - Any exception that generated the error condition. Used to print a stack trace in the log file.
	 * If you pass null, it is the same as calling {@link #logError(String)}
	 */
	public static void logError(String message, Throwable exception) 
	{
		log.logError(message, exception);
	}

	/**
	 * Helper method for logging debug messages to the RSE-style logging file.
	 * This file is located in the .metadata subfolder for this plugin. 
	 * <p>
	 * Debug messages are only logged when running this plugin in the workbench,
	 * and when Logger.DEBUG has been set to true.
	 * 
	 * @param classname - Class issuing the debug message. Typically you pass getClass().getName()
	 * @param message - Message to be written to the log file
	 */
	public static void logDebugMessage(String prefix, String message) 
	{		
		log.logDebugMessage(prefix, message);
	}

	/**
	 * Constructor.
	 */
	public SystemBasePlugin() {
	    super();
	    
	    if (baseInst == null) {
	        baseInst = this;
	    }
	    
		headless = false;
		headlessSet = false;
	}
	
	// ------------------------
	// STATIC HELPER METHODS...
	// ------------------------

    /**
     * Returns the symbolic name of the bundle.
     * @return the symbolic name of the bundle.
     */   
	public String getSymbolicName() {
		return getBundle().getSymbolicName();
	}
	
    /**
	 * Return the fully qualified install directory for this plugin.
	 */
//    protected IPath getInstallLocation() {
//        IPath prefix = null;
//        try
//        {
//	        String filePath = Platform.resolve(getBundle().getEntry("/")).getPath();
//	        prefix = new Path(filePath);
//        }
//        catch (Exception e)
//        {
//            prefix = new Path(getBundle().getEntry("/").getFile());
//        }
// 	   return prefix;
//    }    
	
	// -------------------------------------
	// ABSTRACTUIPLUGIN LIFECYCLE METHODS...
	// -------------------------------------
    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        
        super.start(context);
        
		// logger
	    if (log == null) {
	    	log = LoggerFactory.getLogger(this);
	    	log.logInfo("Loading " + this.getClass());
	    }
    }
    
    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	logDebugMessage(this.getClass().getName(), "SHUTDOWN");
   	    LoggerFactory.freeLogger(this);
        super.stop(context);
    }
    
	/**
	 * Returns the Platform UI workbench.  
	 * <p> 
	 * This method exists as a convenience for plugin implementors.  The
	 * workbench can also be accessed by invoking <code>PlatformUI.getWorkbench()</code>.
	 * </p>
	 * <p>
	 * This is an intercept of the AbstractUIPlugin method, so we can do a try/catch around
	 *  it, as it will throw an exception if we are running headless, in which case the 
	 *  workbench has not even been started.
	 * </p>
	 */
	public IWorkbench getWorkbench() 
	{
		IWorkbench wb = null;
		if (headlessSet && headless) // already been here?
		 	return wb;
		try {
			wb = PlatformUI.getWorkbench();
			
			//wb = super.getWorkbench();
			headless = false;			
		} 
		catch (Exception exc)
		{
			/*
			IDEWorkbenchAdvisor advisor = new IDEWorkbenchAdvisor();
			PlatformUI.createAndRunWorkbench(Display.getDefault(), advisor);
			try
			{
				wb = super.getWorkbench();			
			}
			catch (Exception e)
			{
				headless = true;
			}
			*/
			headless = true;
		}
		headlessSet = true;
		return wb;
	}

    /**
	 * Initialize the image registry by declaring all of the required graphics.
	 * Typically this is a series of calls to putImageInRegistry. Use
	 * getIconPath() to qualify the file name of the icons with their relative
	 * path.
	 */
	protected abstract void initializeImageRegistry();

	/**
     * Construct an image descriptor from a file name and place it in the 
     * image descriptor registry. Actual image construction is delayed until first use.
     * @param id - an arbitrary ID to assign to this image. Used later when retrieving it.
     * @param fileName - the name of the icon file, with extension, relative to this plugin's folder.
     * @return the image descriptor for this particular id.
     * @see SystemBasePlugin#getImage(String);
     */
    protected ImageDescriptor putImageInRegistry(String id, String fileName)
    {
	   ImageDescriptor fid = getPluginImage(fileName);
	   Hashtable t = getImageDescriptorRegistry();
	   t.put(id, fid);
	   return fid;
    }
    
    /**
	 * Retrieve an image descriptor in this plugin's directory tree given its file name. The
	 * file name should be qualified relative to this plugin's bundle. 
	 * For example "icons/myicon.gif"
	 * @parm imagePath the path name to the image relative to this bundle
	 * @return the image descriptor
	 */
	public ImageDescriptor getPluginImage(String imagePath) {
		return getPluginImage(getBundle(), imagePath);
	}

    /**
	 * Retrieves or creates an image based on its id. The image is then stored
	 * in the image registry if it is created so that it may be retrieved again.
	 * Thus, image resources retrieved in this way need not be disposed by the
	 * caller.
	 * 
	 * @parm key the id of the image to retrieve.
	 * @return the Image resource for this id.
	 */
    public Image getImage(String key)
    {
    	// First check the image registry
    	ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get(key);
		if (image == null) { // check the image descriptor registry
			ImageDescriptor descriptor = getImageDescriptor(key);
			if (descriptor != null) {
				imageRegistry.put(key, descriptor);
				image = imageRegistry.get(key);
			} else {
				logError("...error retrieving image for key: " + key);
			}
		}
		return image;
    }
    
    /**
	 * Returns the image descriptor that has been registered to this id.
	 * @parm key the id of the image descriptor to retrieve
	 * @return an ImageDescriptor
	 */
	public ImageDescriptor getImageDescriptor(String key) {
		Hashtable t = getImageDescriptorRegistry();
		ImageDescriptor descriptor = (ImageDescriptor) t.get(key);
		return descriptor;
	}  
	
	/**
	 * Gets the hashtable that is the image descriptor registry. Creates and populates
	 * it if necessary.
	 * @return The image descriptor registry hashtable.
	 */
	private Hashtable getImageDescriptorRegistry() {
		if (imageDescriptorRegistry == null) {
			imageDescriptorRegistry = new Hashtable();
			initializeImageRegistry();
		}
		return imageDescriptorRegistry;
	}

	/**
	 * Returns an image descriptor from the base IDE.
	 * 
	 * @see org.eclipse.ui.views.navigator.ResourceNavigatorActionGroup#getImageDescriptor(java.lang.String)
	 */
	public ImageDescriptor getImageDescriptorFromIDE(String relativePath) 
	{
		Hashtable registry = getImageDescriptorRegistry();
		ImageDescriptor descriptor = (ImageDescriptor)registry.get(relativePath);
		if (descriptor == null) {
			String iconPath = "icons/full/"; //$NON-NLS-1$
			String key = iconPath + relativePath;
			String[] bundleNames = new String[] {"org.eclipse.ui", "org.eclipse.ui.ide"};
			for (int i = 0; (i < bundleNames.length) && (descriptor == null); i++) {
				String bundleName = bundleNames[i];
			    Bundle bundle = Platform.getBundle(bundleName);
			    URL url = bundle.getResource(key);
			    if (url != null) {
			    	descriptor = ImageDescriptor.createFromURL(url);
			    }
			}
			if (descriptor == null) {
				descriptor = ImageDescriptor.getMissingImageDescriptor();
			}
			registry.put(relativePath, descriptor);
		}
		return descriptor;
	}    
            
    // ----------------------------------------
    // TRANSLATABLE RESOURCE-RELATED METHODS...
    // ----------------------------------------
       
    /**
	 * Put up an error message when a programming error is detected.
	 * Please note this should never happen in production so we don't translate!
	 */
    public void showProgrammerErrorMessage(String msg)
    {
	     org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(null);
	     //mb.setTitle("Remote Systems Programming Error");
	     String errmsg = "Programming Error: " + msg;
	     mb.setMessage(errmsg);	
	     mb.open();
	     logError(errmsg);
    }


    // -----------------    
    // LOGGER METHODS...
    // -----------------
          
	/**
     * Get the logger for this plugin. You should not have to directly access
     * the logger, since helper methods are already provided in this class.
     * Use with care.
     */
    public Logger getLogger() 
    {
    	return log;
    }        

	// -------------------------    
	// MISCELLANEOUS METHODS...
	// -------------------------
	
	/**
	 * Return true if we are running in a headless environment. We equate this
	 *  to mean that the workbench is not running.
	 */
	public boolean isHeadless()
	{
		if (!headlessSet)
			getWorkbench();
		return headless;
	}
}