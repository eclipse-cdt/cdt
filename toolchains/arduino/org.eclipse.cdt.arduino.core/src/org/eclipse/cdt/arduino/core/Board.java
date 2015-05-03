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

import java.util.Properties;

public class Board {

	private final String id;
	private final Properties properties;

	public Board(String key, Properties properties) {
		this.id = key;
		this.properties = properties;
	}

	public String getId() {
		return id;
	}

	public String getProperty(String localKey) {
		return properties.getProperty(id + '.' + localKey);
	}

	public String getName() {
		return getProperty("name"); //$NON-NLS-1$
	}

	public String getMCU() {
		return getProperty("build.mcu"); //$NON-NLS-1$
	}

}
