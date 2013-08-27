/********************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation. All rights reserved.
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
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight  (IBM)  - [226086] [dstore][api][breaking] Move ServerLogger class to dstore.core
 * Jacob Garcowski (IBM)  - [232738] [dstore] Delay creation of log file until written to
 * Noriaki Takatsu (IBM)  - [232443] [multithread] A single rsecomm.log for all clients
 * Noriaki Takatsu (IBM)  - [239419] [multithread] Dynamically change the level of logging
 * David McKnight  (IBM)  - [244876] [dstore] make DEBUG a non-final variable of the ServerLogger class
 * David McKnight  (IBM)  - [271914] [dstore] Setting debug on/off dynamically
 * David McKnight  (IBM)  - [269908] [dstore] rsecomm.log file management
 * David McKnight  (IBM)  - [284787] [dstore] ability to disable RSECOMM_LOGFILE_MAX option
 * David McKnight  (IBM)  - [305272] [dstore][multithread] log close in ServerLogger
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 * Noriaki Takatsu (IBM)  - [341578] [dstore] ServerLogger is looped when IOError happens
 * David McKnight  (IBM)  - [351993] [dstore] not able to connect to server if .eclipse folder not available
 * David McKnight  (IBM)  - [366220] Log_To_File no longer default value for log_location in rsecomm.properties
 * David McKnight  (IBM)  - [391774] [dstore] NPE if user-log directory cannot be created
 * David McKnight   (IBM) - [414016] [dstore] new server audit log requirements
 ********************************************************************************/

package org.eclipse.dstore.core.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.ResourceBundle;

import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;

/**
 * Class that facilitates logging for errors, warnings, debug messages and info
 * for DataStore servers.
 *
 * @since 3.0 moved from non-API to API
 */
public class ServerLogger implements IServerLogger
{


	// Constants for logging - for use in rsecomm.properties
	private static final String DEBUG_LEVEL = "debug_level"; //$NON-NLS-1$
	private static final String LOG_LOCATION = "log_location"; //$NON-NLS-1$

	private static final int LOG_WARNING = 1;
	private static final int LOG_INFO = 2;
	private static final int LOG_DEBUG = 3;

	private static final String LOG_TO_STDOUT = "Log_To_StdOut"; //$NON-NLS-1$

	private Object writeLock = new Object();
	private PrintWriter _logFileStream = null;

	/**
	 * Switch to enable debug-level logging. Note that, in 3.0, this variable
	 * was final but, as of 3.0.1, it's not.
	 */
	public static boolean DEBUG = false;

	private int log_level = 0;

	private boolean initialized = false;
	private String logPathName = null;
	private boolean logToFile = true;

	/**
	 * Constructs a new ServerLogger.
	 *
	 * @param logPathName the path on the filesystem to store the log information
	 */
	public ServerLogger(String logPathName) {
		this.logPathName = logPathName;
		// Read .properties file to configure
		try {
			ResourceBundle properties = ResourceBundle.getBundle("rsecomm"); //$NON-NLS-1$
			String debug_level = properties.getString(DEBUG_LEVEL).trim();
			log_level = Integer.parseInt(debug_level);
			String log_location = properties.getString(LOG_LOCATION).trim();
			if (log_location.equalsIgnoreCase(LOG_TO_STDOUT)) {
				logToFile = false;
				_logFileStream = new PrintWriter(System.out);
			}
		} catch (Exception e) {
			// Just use logging defaults: log_level = 0, log to file
			//e.printStackTrace();		
		}
	}

	private void initialize()
	{
		initialized = true;
		if (_logFileStream == null) {
			if (logToFile) {
				try {
			  		File _logFile = getLogFile("rsecomm"); //$NON-NLS-1$
			  		if (_logFile != null){
				  		if (!_logFile.exists()){
				  			_logFile.createNewFile();
				  		}
				  		if (_logFile != null && _logFile.canWrite()){
				  			_logFileStream = new PrintWriter(new FileOutputStream(_logFile));
				  		}
				  		else {
				  			log_level = 0;
							logToFile = false;
							_logFileStream = new PrintWriter(System.out);
				  		}
			  		}
			  		else { // no log file, default to stdout
			  			System.out.println("No log file access " + logPathName + "rsecomm.log");		 //$NON-NLS-1$ //$NON-NLS-2$
				 		log_level = 0;
				 		logToFile = false;
				 		_logFileStream = new PrintWriter(System.out);	
			  		}	
				} catch (IOException e) {
					System.out.println("Error opening log file " + logPathName + "rsecomm.log");		 //$NON-NLS-1$ //$NON-NLS-2$
					log_level = 0;
					logToFile = false;
					_logFileStream = new PrintWriter(System.out);
				}
			}
		}
	}

	/**
	 * closeLogFileStream
	 * @since 3.2
	 */
	public void closeLogFileStream(){
		if (_logFileStream != null){
			_logFileStream.close();
		}
	}
	
	private File getLogFile(String preferredName){
		String ext = ".log"; //$NON-NLS-1$
		boolean found = false;
		long logFileMax = 1000000;
		String logFileMaxStr = System.getProperty(IDataStoreSystemProperties.RSECOMM_LOGFILE_MAX);
		if (logFileMaxStr != null && logFileMaxStr.length() > 0){
			try {
				logFileMax = Integer.parseInt(logFileMaxStr);
			}
			catch (NumberFormatException e){
				System.err.println("ServerLogger: "+e.toString()); //$NON-NLS-1$
			}
		}
		
		File logFile = null;
		String name = null;
		int suffix = 0;
		while (!found) {
			name = (suffix == 0) ? preferredName + ext: preferredName + suffix + ext;
			logFile = new File(logPathName, name);

			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
					found = true;
				}
				catch (IOException e){
					return null;
				}
			}
			else {
				// if the file exists, check it's size
				long fileSize = logFile.length();
				if  (logFileMax > 0 && fileSize > logFileMax){ // if logFileMax is 0 or less, than always use the same file					
					// file too big, need a new one				
					suffix++;
				}
				else {
					found = true;
				}
			}	
		}
		return logFile;
	}
	

	/**
	 * Logs an informational message
	 *
	 * @param minerName the name of the miner associated with this message
	 * @param message Message text to be logged.
	 */
	public void logInfo(String minerName, String message) {
		if (!initialized)
			initialize();
		String loggerLogLevel = System.getProperty(IDataStoreSystemProperties.DSTORE_LOGGER_LOG_LEVEL);
		if (loggerLogLevel != null){
			try {
				log_level = Integer.parseInt(loggerLogLevel);
			}
			catch (NumberFormatException e){
				System.err.println("ServerLogger: "+e.toString()); //$NON-NLS-1$
			}
		}
		if (log_level >= LOG_INFO) {
			if (_logFileStream != null) {
				synchronized(writeLock) {
					try {
						_logFileStream.println(new Date());
						_logFileStream.println("INFO " + minerName + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
						_logFileStream.println("---------------------------------------------------------------"); //$NON-NLS-1$
						_logFileStream.flush();
					}catch (Exception e) {}
				}
			}
		}
	}


	/**
	 * Logs a warning message
	 *
	 * @param minerName the name of the miner associated with this message
	 * @param message Message text to be logged.
	 */
	public void logWarning(String minerName, String message) {
		if (!initialized)
			initialize();
		String loggerLogLevel = System.getProperty(IDataStoreSystemProperties.DSTORE_LOGGER_LOG_LEVEL); 
		if (loggerLogLevel != null){
			try {
				log_level = Integer.parseInt(loggerLogLevel);
			}
			catch (NumberFormatException e){
				System.err.println("ServerLogger: "+e.toString()); //$NON-NLS-1$
			}
		}
		if (log_level >= LOG_WARNING) {
			if (_logFileStream != null) {
				synchronized(writeLock) {
					try {
						_logFileStream.println(new Date());
						_logFileStream.println("WARNING " + minerName + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
						_logFileStream.println("---------------------------------------------------------------"); //$NON-NLS-1$
						_logFileStream.flush();
					}catch (Exception e) {}
				}
			}
		}
	}


	/**
	 * Logs an error message
	 *
	 * @param minerName the name of the miner associated with this message
	 * @param message Message text to be logged.
	 *
	 * @param exception Exception that generated the error.  Used to print a stack trace.
	 */
	public void logError(String minerName, String message, Throwable exception) {
		if (!initialized)
			initialize();
		
		String loggerLogLevel = System.getProperty(IDataStoreSystemProperties.DSTORE_LOGGER_LOG_LEVEL);
		if (loggerLogLevel != null){
			try {
				log_level = Integer.parseInt(loggerLogLevel);
			}
			catch (NumberFormatException e){
				System.err.println("ServerLogger: "+e.toString()); //$NON-NLS-1$
			}
		}		
		
		if (_logFileStream != null) {
			synchronized(writeLock) {
				try {
					_logFileStream.println(new Date());
					_logFileStream.println("ERROR " + minerName + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
					if (exception != null) {
						exception.printStackTrace(_logFileStream);
					}
					_logFileStream.println("---------------------------------------------------------------"); //$NON-NLS-1$
					_logFileStream.flush();
				}catch (Exception e) {}
			}
		}
	}


	/**
	 * Logs a debug message
	 *
	 * @param minerName the name of the miner associated with this message
	 * @param message Message text to be logged.
	 */
	public synchronized void logDebugMessage(String minerName, String message) {
		if (!initialized)
			initialize();
		
		String loggerLogLevel = System.getProperty(IDataStoreSystemProperties.DSTORE_LOGGER_LOG_LEVEL); 
		if (loggerLogLevel != null){
			try {
				log_level = Integer.parseInt(loggerLogLevel);
			}
			catch (NumberFormatException e){
				System.err.println("ServerLogger: "+e.toString()); //$NON-NLS-1$
			}
		}
		if (DEBUG && log_level == LOG_DEBUG) {
			if (_logFileStream != null) {
				synchronized(writeLock) {
					try {
						_logFileStream.println(new Date());
						_logFileStream.println("DEBUG " + minerName + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
						_logFileStream.println("---------------------------------------------------------------"); //$NON-NLS-1$
						_logFileStream.flush();
					}catch (Exception e) {}
				}
			}
		}
	}

	/**
	 * logAudit
	 * 
	 * @param data information to log.
	 */
	public void logAudit(String[] data){	
		// initial implementation is a no-op but extenders (i.e. zosServerLogger) can provide 
		// required function
	}
}