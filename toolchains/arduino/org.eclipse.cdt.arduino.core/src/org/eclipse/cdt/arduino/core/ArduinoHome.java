/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core;

import java.io.File;

import org.eclipse.core.runtime.Platform;

public class ArduinoHome {

	public static final String preferenceName = "arduinoHome"; //$NON-NLS-1$
	private static final String qualifiedName = "org.eclipse.cdt.arduino.ui"; //$NON-NLS-1$

	public static File getArduinoHome() {
		String arduinoHome = Platform.getPreferencesService().getString(qualifiedName, preferenceName, getDefault(),
				null);
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			arduinoHome += "/Contents/Java"; //$NON-NLS-1$
		}
		return new File(arduinoHome);
	}

	public static String getDefault() {
		switch (Platform.getOS()) {
		case Platform.OS_MACOSX:
			return "/Applications/Arduino.app"; //$NON-NLS-1$
		case Platform.OS_WIN32:
			return "C:\\Program Files (x86)\\Arduino"; //$NON-NLS-1$
		default:
			return ""; //$NON-NLS-1$
		}
	}

}
