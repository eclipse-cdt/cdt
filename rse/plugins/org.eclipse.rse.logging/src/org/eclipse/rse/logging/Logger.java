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
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

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
 *             //out.logDebugMessage(<br>
 *             //	"myPlugin",<br>
 *             //	"this is a debug message from class myPlugin.");<br>
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

	private ILog systemsPluginLog = null;
	private RemoteSystemLogListener logListener = null;
	private String pluginId = null;
	private Plugin systemPlugin = null;
	private int debug_level = IRemoteSystemsLogging.LOG_ERROR;

	/**
	 * Creates a new Logger. Invoked by the LoggerFactory.
	 * @param systemPlugin The preferences for this plugin will determine the detail 
	 * logged by this logger. This allows different levels of detail to be logged in the 
	 * workbench.
	 * @see LoggerFactory#getInst(Plugin);
	 */
	Logger(Plugin systemPlugin) {
		this.systemPlugin = systemPlugin;
		this.pluginId = systemPlugin.getBundle().getSymbolicName();
		initialize();
	}

	/**
	 * Initialize the Logger. The logger uses an ILog from the platform for this particular plugin, and
	 * establishes a listener on that log to format the items placed in the log. 
	 */
	private void initialize() {
		systemsPluginLog = systemPlugin.getLog();
		logListener = new RemoteSystemLogListener(systemPlugin);
		systemsPluginLog.addLogListener(logListener);
		Preferences store = systemPlugin.getPluginPreferences();
		debug_level = store.getInt(IRemoteSystemsLogging.DEBUG_LEVEL);
		store.addPropertyChangeListener(this);
		store.addPropertyChangeListener(logListener);
	}

	/**
	 * Log a Debug message. This is intended to be used as follows:<br>
	 * Logger.logDebugMessage("someClassName", "someMessage");<br>
	 * <br>
	 * and the output will be:<br>
	 * <br>
	 * ---------------------------------------------------------------<br>
	 * DEBUG  org.eclipse.rse.logging   someClassName<br>
	 *   someMessage<br>
	 * ---------------------------------------------------------------<br>
	 * <br>
	 * <br>
	 * Note that since this message is only for developer debugging, it does not 
	 * need to be localized to proper local.<br>
	 */
	public synchronized void logDebugMessage(String className, String message) {
		if (debug_level >= IRemoteSystemsLogging.LOG_DEBUG) {
			MultiStatus debugStatus = new MultiStatus(pluginId, IStatus.OK, className, null);
			Status infoStatus = new Status(IStatus.OK, pluginId, IStatus.OK, message, null);
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
		if (debug_level >= IRemoteSystemsLogging.LOG_ERROR) {
			if (message == null) message = "";
			Status errorStatus = new Status(IStatus.ERROR, pluginId, IStatus.OK, message, ex);
			systemsPluginLog.log(errorStatus);
		}
	}

	/** 
	 * Log an Information message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logInfo(String message) {
		logInfo(message, null);
	}

	/** 
	 * Log an Information message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logInfo(String message, Throwable ex) {
		if (debug_level >= IRemoteSystemsLogging.LOG_INFO) {
			if (message == null) message = "";
			Status infoStatus = new Status(IStatus.INFO, pluginId, IStatus.OK, message, ex);
			systemsPluginLog.log(infoStatus);
		}
	}

	/** 
	 * Log a Warning message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logWarning(String message) {
		logWarning(message, null);
	}

	/** 
	 * Log a Warning message. Note that the message should already 
	 * be localized to proper local.<br>
	 * ie: Resource.getString() should already have been called
	 */
	public synchronized void logWarning(String message, Throwable ex) {
		if (debug_level >= IRemoteSystemsLogging.LOG_WARNING) {
			if (message == null) message = "";
			Status warningStatus = new Status(IStatus.WARNING, pluginId, IStatus.OK, message, ex);
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
	 * Handle changes from a Preferences page.
	 */
	public synchronized void propertyChange(PropertyChangeEvent event) {
		Preferences prefs = systemPlugin.getPluginPreferences();
		debug_level = prefs.getInt(IRemoteSystemsLogging.DEBUG_LEVEL);
	}

}