/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.arduino.core.internal.messages"; //$NON-NLS-1$
	public static String ArduinoBoardManager_0;
	public static String ArduinoBoardManager_1;
	public static String ArduinoBuildConfigurationProvider_UnknownConnection;
	public static String ArduinoLaunchConfigurationDelegate_0;
	public static String ArduinoLaunchConfigurationDelegate_1;
	public static String ArduinoLaunchConfigurationDelegate_2;
	public static String ArduinoManager_0;
	public static String ArduinoManager_1;
	public static String ArduinoManager_2;
	public static String ArduinoPlatform_0;
	public static String ArduinoPlatform_1;
	public static String ArduinoProjectGenerator_0;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
