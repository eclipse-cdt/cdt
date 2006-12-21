/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.internal;

import java.util.Properties;

import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;

/**
 * RSE connection properties implementation.
 */
public class RSEConnectionProperties implements IRSEConnectionProperties {
	private final Properties properties;

	/**
	 * Constructor.
	 * 
	 * @param properties The string based properties container. Must be not <code>null</code>.
	 */
	public RSEConnectionProperties(Properties properties) {
		super();
		
		assert properties != null;
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionProperties#getProperty(java.lang.String)
	 */
	public String getProperty(String key) {
		assert key != null;
		String value = properties.getProperty(key, null);
		return value != null ? value.trim() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionProperties#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String key, String value) {
		assert key != null;
		if (value != null) {
			properties.setProperty(key, value);
		} else {
			properties.remove(key);
		}
	}
}
