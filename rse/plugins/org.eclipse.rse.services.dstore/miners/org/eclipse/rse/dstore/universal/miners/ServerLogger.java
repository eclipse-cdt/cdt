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

package org.eclipse.rse.dstore.universal.miners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.ResourceBundle;


public class ServerLogger {
	
	
	// Constants for logging - for use in rsecomm.properties
	private static final String DEBUG_LEVEL = "debug_level";
	private static final String LOG_LOCATION = "log_location";
	
	private static final int LOG_WARNING = 1;
	private static final int LOG_INFO = 2;
	private static final int LOG_DEBUG = 3;
	
	private static final String LOG_TO_STDOUT = "Log_To_StdOut";

	private static Object writeLock = new Object();
	private static PrintWriter _logFileStream = null;
	
	public static final boolean DEBUG = false;
	private static int log_level = 0;

	/**
	 * 
	 */
	public ServerLogger(String logPathName) {
		if (_logFileStream == null) {
			// Read .properties file to configure
			boolean logToFile = true;
			
			try { 
				ResourceBundle properties = ResourceBundle.getBundle("rsecomm");
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
				
			if (logToFile) {
				try {
			  		File _logFile = new File(logPathName, "rsecomm.log");
	 	 		
	 	 			if (!_logFile.exists()) {
	  					_logFile.createNewFile();
	  				}
	  		
	  				_logFileStream = new PrintWriter(new FileOutputStream(_logFile));
	  		
				} catch (IOException e) {
					System.out.println("Error opening log file " + logPathName + "rsecomm.log");		
				}
			}
		}
	}
	
	
	/**
	 * logInfo
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 */
	public static void logInfo(String minerName, String message) {
		if (log_level >= LOG_INFO) {
			if (_logFileStream != null) {
				synchronized(writeLock) {
					try {
						_logFileStream.println(new Date());
						_logFileStream.println("INFO " + minerName + ": " + message);
						_logFileStream.println("---------------------------------------------------------------");
						_logFileStream.flush();
					}catch (Exception e) {}
				}
			}
		}
	}


	/**
	 * logWarning
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 */
	public static void logWarning(String minerName, String message) {
		if (log_level >= LOG_WARNING) {
			if (_logFileStream != null) {
				synchronized(writeLock) {
					try {
						_logFileStream.println(new Date());
						_logFileStream.println("WARNING " + minerName + ": " + message);
						_logFileStream.println("---------------------------------------------------------------");
						_logFileStream.flush();
					}catch (Exception e) {}
				}
			}
		}
	}
	
	
	/**
	 * logError
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 * 
	 * @param exception Exception that generated the error.  Used to print a stack trace.
	 */
	public static void logError(String minerName, String message, Throwable exception) {
		if (_logFileStream != null) {
			synchronized(writeLock) {
				try {
					_logFileStream.println(new Date());
					_logFileStream.println("ERROR " + minerName + ": " + message);
					if (exception != null) {
						exception.printStackTrace(_logFileStream);
					}
					_logFileStream.println("---------------------------------------------------------------");
					_logFileStream.flush();
				}catch (Exception e) {}
			}
		}
	}


	/**
	 * logDebugMessage
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 */
	public synchronized static void logDebugMessage(String minerName, String message) {
		if (DEBUG && log_level == LOG_DEBUG) {
			if (_logFileStream != null) {
				synchronized(writeLock) {
					try {
						_logFileStream.println(new Date());
						_logFileStream.println("DEBUG " + minerName + ": " + message);
						_logFileStream.println("---------------------------------------------------------------");
						_logFileStream.flush();
					}catch (Exception e) {}
				}
			}
		}
	}

}