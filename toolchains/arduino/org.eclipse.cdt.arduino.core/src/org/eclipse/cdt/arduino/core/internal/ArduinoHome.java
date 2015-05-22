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
package org.eclipse.cdt.arduino.core.internal;

import java.io.File;

import org.eclipse.core.runtime.Platform;

public class ArduinoHome {

	public static File getArduinoDir() {
		switch (Platform.getOS()) {
		case Platform.OS_MACOSX:
			return new File("/Applications/Arduino.app/Contents/Java"); //$NON-NLS-1$
		case Platform.OS_WIN32:
			return new File("C:\\Program Files (x86)\\Arduino"); //$NON-NLS-1$
		default:
			return null;
		}
	}

}
