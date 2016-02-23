/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.remote;

import org.eclipse.launchbar.remote.core.RemoteLaunchTargetProvider;

public class ArduinoLaunchTargetProvider extends RemoteLaunchTargetProvider {

	@Override
	protected String getTypeId() {
		return ArduinoRemoteConnection.TYPE_ID;
	}

}
