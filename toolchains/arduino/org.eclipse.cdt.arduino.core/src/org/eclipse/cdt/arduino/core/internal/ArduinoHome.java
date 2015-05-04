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
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Platform;

public class ArduinoHome {

	private static File home;
	
	public static File get() {
		if (home == null) {
			String arduinoPathStr = System.getProperty("org.eclipse.cdt.arduino.home"); //$NON-NLS-1$
			if (arduinoPathStr != null) {
				home = new File(arduinoPathStr);
			} else {
				try {
					home = new File(new File(Platform.getInstallLocation().getURL().toURI()), "arduino"); //$NON-NLS-1$
				} catch (URISyntaxException e) {
					// TODO log
					e.printStackTrace();
					home = new File("nohome"); //$NON-NLS-1$
				}
			}
		}
		return home;
	}

}
