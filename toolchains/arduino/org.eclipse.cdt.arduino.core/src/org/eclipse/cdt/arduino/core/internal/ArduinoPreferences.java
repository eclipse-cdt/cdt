/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class ArduinoPreferences {

	private static final String ARDUINO_HOME = "arduinoHome"; //$NON-NLS-1$
	private static final String BOARD_URLS = "boardUrls"; //$NON-NLS-1$

	private static final String defaultHome = Paths.get(System.getProperty("user.home"), ".arduinocdt").toString(); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String defaultBoardUrls = "http://downloads.arduino.cc/packages/package_index.json" //$NON-NLS-1$
			+ "\nhttps://adafruit.github.io/arduino-board-index/package_adafruit_index.json"; //$NON-NLS-1$

	private static IEclipsePreferences getPrefs() {
		return InstanceScope.INSTANCE.getNode(Activator.getId());
	}

	public static Path getArduinoHome() {
		return Paths.get(getPrefs().get(ARDUINO_HOME, defaultHome));
	}

	public static void setArduinoHome(Path home) {
		IEclipsePreferences prefs = getPrefs();
		prefs.put(ARDUINO_HOME, home.toString());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	public static String getBoardUrls() {
		return getPrefs().get(BOARD_URLS, defaultBoardUrls);
	}

	public static URL[] getBoardUrlList() throws CoreException {
		List<URL> urlList = new ArrayList<>();
		for (String url : getBoardUrls().split("\n")) { //$NON-NLS-1$
			try {
				urlList.add(new URL(url.trim()));
			} catch (MalformedURLException e) {
				throw Activator.coreException(e);
			}
		}
		return urlList.toArray(new URL[urlList.size()]);
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

	public static void setBoardUrlList(URL[] urls) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < urls.length - 1; ++i) {
			str.append(urls[i].toString());
			str.append('\n');
		}
		if (urls.length > 0) {
			str.append(urls[urls.length - 1].toString());
		}
		setBoardUrls(str.toString());
	}

	public static String getDefaultArduinoHome() {
		return defaultHome;
	}

	public static String getDefaultBoardUrls() {
		return defaultBoardUrls;
	}
}
