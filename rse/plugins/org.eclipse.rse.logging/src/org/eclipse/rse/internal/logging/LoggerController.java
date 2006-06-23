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

package org.eclipse.rse.internal.logging;

import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.rse.logging.Logger;

public class LoggerController {

	private static Hashtable pluginTable = new Hashtable();

	/**
	 * Return an previously cached Logger instance.<br>
	 * It will return null if no Logger instance has been created
	 * for this plugin before.
	 */
	public static Logger getInst(Plugin plugin) {
		if (pluginTable.containsKey(plugin))
			return (Logger) pluginTable.get(plugin);
		else
			return null;
	}

	public static void registerInst(Plugin plugin, Logger logger) {
		pluginTable.put(plugin, logger);
		return;
	}

	public static void freeInst(Plugin plugin) {
		// get cached instance if one exists.
		Logger logger = getInst(plugin);
		// no luck, this means we have an incorrect free, do nothing.
		if (logger == null)
			return;
		logger.freeResources();
		pluginTable.remove(plugin);
		return;

	}

}