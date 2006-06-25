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

import java.util.Hashtable;
import org.eclipse.core.runtime.Plugin;

/**
 * Factory class for creating Logger instances.<br>
 * Keep in mind that this factory class follows the singleton model.<br>
 * ie: once an instance of a Logger class for a given plugin is created, 
 * it will always be reused. 
 */
public class LoggerFactory {

	private static Hashtable pluginTable = new Hashtable();

	/**
	 * Returns the Logger instance for a given plugin. There is only
	 * one instance of the Logger class per plugin. 
	 */
	public static Logger getLogger(Plugin plugin) {
		Logger logger = (Logger) pluginTable.get(plugin);
		if (logger == null) {
			logger = new Logger(plugin);
			pluginTable.put(plugin, logger);
		}
		return logger;
	}

	/**
	 * Frees resources used by the Logger instance for the given plugin.<br>
	 * This methods must be called as part of the the plugin shutdown life cycle.
	 */
	public static void freeLogger(Plugin plugin) {
		Logger logger = (Logger) pluginTable.get(plugin);
		if (logger != null) {
			logger.freeResources();
			pluginTable.remove(plugin);
		}
	}

}