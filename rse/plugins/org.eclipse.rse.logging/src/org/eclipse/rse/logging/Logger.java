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

package org.eclipse.rse.logging;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.rse.internal.logging.RemoteSystemLogListener;
import org.osgi.framework.Bundle;


/**
 * Generic Logger class for handling Remote Systems logging and tracing.<br>
 * <br>
 * The debug level is determined by a "debug_level" key in the preferences store 
 * of the plugin that owns this Logger instance.<br>
 * <br>
 * The debug location is determined by a "log_location" key in the preferences store 
 * of the plugin that owns this Logger instance.<br>  
 * <br>
 * The valid values for these keys can be found in the javadocs for IRemoteSystemsLogging.<br>.  
 * This means that these keys could have been defined through hardcoding in your 
 * plugin startup code, through preferences.ini in the plugin install directory, 
 * OR from pref_store.ini in the plugin read/write metadata area. <br> 
 * The default behavior is to log to file, and to log only errors.
 * <br>
 * A typical usage of this class is as follows: <br>
 * <br>
 *      public class myPlugin extends AbstractUIPlugin { <br>
 * <br>
 *        // a cached Logger inst for convenience.<br>
 *        public static Logger out = null;<br>
 * <br>
 *        public myPlugin(IPluginDescriptor descriptor) { <br>
 *             super(descriptor);<br>
 *             ......<br>
 *             ......<br>
 *             out = LoggerFactory.getInst(this);<br>
 *             out.logInfo("loading myPlugin class.");<br>
 *             //out.logWarning("This is a warning message.");<br>
 *             //out.logError("This is an error.", new Exception());<br>
 *             //if (Logger.DEBUG)<br>
 *             //	out.logDebugMessage(<br>
 *             //		"myPlugin",<br>
 *             //		"this is a debug message from class myPlugin.");<br>
 *             ......<br>
 *             ......<br>
 *        }<br>
 * <br>
 * <br>
 *         public void shutdown() throws CoreException {<br>
 *              super.shutdown();<br>
 *              LoggerFactory.freeInst(this);<br>
 *         }<br>
 * <br>
 * 
 */
public class Logger implements IPropertyChangeListener {

	public static final String Copyright =
		"(C) Copyright IBM Corp. 2002, 2003.  All Rights Reserved.";

	/**
	 * This SHOULD be set to false in production.<br>
	 * Used to compile out developement debug messages.<br>
	 */
	public final static boolean DEBUG = false;

	// Cashed workbenchPlugin Log, LogListener instances
	private ILog systemsPluginLog = null;
	private RemoteSystemLogListener logListener = null;

	// Cashed Plugin ID, and plugin 
	private String pluginId = null;
	Plugin systemPlugin = null;

	// Controls logging level
	private int debug_level = 0;

	// Captures initialization errors
	private boolean init_ok = true;

	protected Logger(Plugin systemPlugin) {
		this.systemPlugin = systemPlugin;
		this.pluginId = systemPlugin.getBundle().getSymbolicName();
		initialize();
	}

	private void initialize() {
		try {

			systemsPluginLog = systemPlugin.getLog();
			if (logListener == null)
				logListener = new RemoteSystemLogListener(systemPlugin);
			systemsPluginLog.addLogListener(logListener);

			// get the debug level from plugin Preference store.
			// note: logListener must be initialized before calling getPreference store!
			Preferences store = systemPlugin.getPluginPreferences();
			debug_level = store.getInt(IRemoteSystemsLogging.DEBUG_LEVEL);

			store.addPropertyChangeListener(this);
			store.addPropertyChangeListener(logListener);

		} catch (Exception e) {
			// Errors occured during initialize, disable logging.
			// should never be here. Use Platform logger instead.
		    Bundle bundle = Platform.getBundle(Platform.PI_RUNTIME);
			Platform.getLog(bundle).log(
				new Status(
					IStatus.ERROR,
					IRemoteSystemsLogging.PLUGIN_ID,
					IStatus.OK,
					"could not create Logger for " + pluginId,
					e));
			init_ok = false;
		}
	}

	/**
	 * Log a Debug message. This is intended to be wrapped as follows:<br>
	 * if (Logger.DEBUG)<br>
	 *      Logger.logDebugMessage("someClassName", "someMessage");<br>
	 * <br>
	 * and the output will be:<br>
	 * <br>
	 * ---------------------------------------------------------------<br>
	 * DEBUG  com.ibm.etools.systems.logging   someClassName<br>
	 *   someMessage<br>
	 * ---------------------------------------------------------------<br>
	 * <br>
	 * <br>
	 * Note that since this message is only for developer debugging, it does not 
	 * need to be localized to proper local.<br>
	 */

	public synchronized void logDebugMessage(
		String className,
		String message) {
		if ((init_ok) && (debug_level >= IRemoteSystemsLogging.LOG_DEBUG)) {
			// ie: print all INFO, WARNING and ERROR messages
			MultiStatus debugStatus =
				new MultiStatus(pluginId, IStatus.OK, className, null);
			Status infoStatus =
				new Status(IStatus.OK, pluginId, IStatus.OK, message, null);
			debugStatus.add(infoStatus);
			systemsPluginLog.log(debugStatus);
		}
	}

	/** 
	 * Log an Error message with an exception. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */

	public synchronized void logError(String message, Throwable ex) {
		if ((init_ok) && (debug_level >= IRemoteSystemsLogging.LOG_ERROR)) {
			// ie: print only ERROR messages
			if (message == null)
				message = "";
			Status errorStatus =
				new Status(IStatus.ERROR, pluginId, IStatus.OK, message, ex);
			systemsPluginLog.log(errorStatus);

		}
	}

	/** 
	 * Log an Information message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logInfo(String message) 
	{
		logInfo(message, null);
	}
	
	/** 
	 * Log an Information message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */

	public synchronized void logInfo(String message, Throwable ex) {
		if ((init_ok) && (debug_level >= IRemoteSystemsLogging.LOG_INFO)) {
			if (message == null)
				message = "";
			// ie: print all INFO, WARNING and ERROR messages
			Status infoStatus =
				new Status(IStatus.INFO, pluginId, IStatus.OK, message, ex);
			systemsPluginLog.log(infoStatus);

		}

	}

	/** 
	 * Log a Warning message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logWarning(String message) 
	{
		logWarning(message, null);
	}

	/** 
	 * Log a Warning message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logWarning(String message, Throwable ex) {
		if ((init_ok) && (debug_level >= IRemoteSystemsLogging.LOG_WARNING)) {
			if (message == null)
				message = "";
			// ie: print all WARNING and ERROR messages
			Status warningStatus =
				new Status(
					IStatus.WARNING,
					pluginId,
					IStatus.OK,
					message,
					ex);
			systemsPluginLog.log(warningStatus);
		}

	}

	public synchronized void setDebugLevel(int level) {
		debug_level = level;
	}
	
	public synchronized int getDebugLevel() {
		return debug_level;
	}

	public synchronized void freeResources() {
		logListener.freeResources();
	}

	/**
	 * Handle changes from Preferences page.
	 */
	public synchronized void propertyChange(PropertyChangeEvent event) {
		// refresh the debug level from plugin Preference store
		Preferences prefs = systemPlugin.getPluginPreferences();
		debug_level = prefs.getInt(IRemoteSystemsLogging.DEBUG_LEVEL);
	}

}