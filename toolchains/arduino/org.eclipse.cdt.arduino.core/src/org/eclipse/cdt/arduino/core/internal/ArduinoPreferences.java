/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class ArduinoPreferences {

	private static final String ARDUINO_HOME = "arduinoHome"; //$NON-NLS-1$
	private static final String BOARD_URLS = "boardUrls"; //$NON-NLS-1$

	private static final String defaultHome = Paths.get(System.getProperty("user.home"), ".arduinocdt").toString(); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String defaultBoardUrls = "http://downloads.arduino.cc/packages/package_index.json" //$NON-NLS-1$
			+ "\nhttp://arduino.esp8266.com/stable/package_esp8266com_index.json" //$NON-NLS-1$
			+ "\nhttps://adafruit.github.io/arduino-board-index/package_adafruit_index.json"; //$NON-NLS-1$

	private static IEclipsePreferences getPrefs() {
		return InstanceScope.INSTANCE.getNode(Activator.getId());
	}

	public static Path getArduinoHome() {
		return Paths.get(getPrefs().get(ARDUINO_HOME, defaultHome));
	}

	public static String getBoardUrls() {
		return getPrefs().get(BOARD_URLS, defaultBoardUrls);
	}

	public static void setBoardUrls(String boardUrls) {
		IEclipsePreferences prefs = getPrefs();
		prefs.put(BOARD_URLS, boardUrls);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	public static String getDefaultBoardUrls() {
		return defaultBoardUrls;
	}
}
