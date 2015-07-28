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

import org.eclipse.core.runtime.Platform;

public class ArduinoPreferences {

	public static String ARDUINO_HOME = "arduinoHome"; //$NON-NLS-1$

	public static Path getArduinoHome() {
		String pathStr = Platform.getPreferencesService().getString(Activator.getId(), ARDUINO_HOME, null, null);
		return pathStr != null ? Paths.get(pathStr) : getDefaultArduinoHome();
	}

	public static Path getDefaultArduinoHome() {
		return Paths.get(System.getProperty("user.home"), ".arduinocdt"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
