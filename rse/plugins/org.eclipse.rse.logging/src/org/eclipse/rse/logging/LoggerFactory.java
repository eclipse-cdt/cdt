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

package org.eclipse.rse.logging;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.rse.internal.logging.LoggerController;

/**
 * Factory class for creating Logger instances.<br>
 * Keep in mind that this factory class follows the singleton model.<br>
 * ie: once an instance of a Logger class for a given plugin is created, 
 * it will always be reused. 
 */
public class LoggerFactory {


	/**
	 * Returns a Logger instance for the given plugin.<br> Note that there is only
	 * a singelton instance of the Logger class per plugin. You are guarenteed the 
	 * same instance if one has previously been created. 
	 */
	public static Logger getInst(Plugin plugin) {

		// get cached instance from controller if one exists.
		Logger inst = LoggerController.getInst(plugin);
		// no luck, create it and register it with the controller, and create 
		// preference page.
		if (inst == null) {
			inst = new Logger(plugin);
			LoggerController.registerInst(plugin, inst);
			// Check to see if the Logging plugin out instance has been created yet.
			// If it has, use it to log
			if (RemoteSystemsLoggingPlugin.out != null)
				RemoteSystemsLoggingPlugin.out.logInfo(
					"Created Logger instance for "
						+ plugin.getBundle().getSymbolicName());
		}

		return inst;
	}

	/**
	* Frees resources used by the Logger instance for the given plugin.<br>
	* This methods must be called as part of the the plugin shutdown life cycle.
	*/
	public static void freeInst(Plugin plugin) {
		// delegate to controller	
		LoggerController.freeInst(plugin);
	}

}