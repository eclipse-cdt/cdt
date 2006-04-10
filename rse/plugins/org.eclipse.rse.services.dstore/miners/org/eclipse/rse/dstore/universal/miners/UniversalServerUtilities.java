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

public class UniversalServerUtilities {


	private static String _userPreferencesDirectory = null;
	private static ServerLogger log = new ServerLogger(getUserPreferencesDirectory());
	
	
	/** 
	 * getUserPreferencesDirectory() - returns directory on IFS where to store user settings
	 */
	public static String getUserPreferencesDirectory()
	{
		if (_userPreferencesDirectory == null) {
			
			_userPreferencesDirectory = System.getProperty("user.home");
			String userID = System.getProperty("user.name");
			
 			// append a '/' if not there
  			if ( _userPreferencesDirectory.length() == 0 || 
  			     _userPreferencesDirectory.charAt( _userPreferencesDirectory.length() -1 ) != File.separatorChar ) {
  			     
				_userPreferencesDirectory = _userPreferencesDirectory + File.separator;
		    }
  		
  			_userPreferencesDirectory = _userPreferencesDirectory + ".eclipse" + File.separator + 
  			         												"RSE" + File.separator + userID + File.separator;
	  		File dirFile = new File(_userPreferencesDirectory);
	  		if (!dirFile.exists()) {
	 	 		dirFile.mkdirs();
	  		}
		}
	  	
	  return _userPreferencesDirectory;
	}

	/**
	 * logInfo
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 */
	public static void logInfo(String minerName, String message) {
		ServerLogger.logInfo(minerName, message);
	}

	/**
	 * logWarning
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 */
	public static void logWarning(String minerName, String message) {
		ServerLogger.logWarning(minerName, message);
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
		ServerLogger.logError(minerName, message, exception);
	}

	/**
	 * logDebugMessage
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 */
	public static void logDebugMessage(String minerName, String message) {
		ServerLogger.logDebugMessage(minerName, message);
	}

}