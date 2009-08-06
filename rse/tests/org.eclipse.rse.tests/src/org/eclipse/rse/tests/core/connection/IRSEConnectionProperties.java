/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - Support REXEC launch type for dstore
 *******************************************************************************/
package org.eclipse.rse.tests.core.connection;

/**
 * Interface declares public access and management methods to deal
 * with the RSE connection specific properties.
 */
public interface IRSEConnectionProperties {

	public final String ATTR_NAME = "name"; //$NON-NLS-1$
	public final String ATTR_PROFILE_NAME = "profile_name"; //$NON-NLS-1$
	public final String ATTR_SYSTEM_TYPE_ID = "system_type_id"; //$NON-NLS-1$
	public final String ATTR_ADDRESS = "address"; //$NON-NLS-1$
	public final String ATTR_USERID = "userid"; //$NON-NLS-1$
	public final String ATTR_PASSWORD = "password"; //$NON-NLS-1$
	public final String ATTR_DAEMON_PORT = "daemon_port"; //$NON-NLS-1$
	public final String ATTR_REXEC_PORT = "rexec_port"; //$NON-NLS-1$
	public final String ATTR_SERVER_LAUNCH_TYPE = "launch_type"; //$NON-NLS-1$
	public final String ATTR_SERVER_PATH = "dstore_server_path"; //$NON-NLS-1$
	public final String ATTR_SERVER_SCRIPT = "dstore_server_script"; //$NON-NLS-1$

	/**
	 * Returns the associated property stored under the specified key.
	 *
	 * @param key The property key. Must be not <code>null</code>.
	 * @return The properties value or <code>null</code> if not set.
	 */
	public String getProperty(String key);

	/**
	 * Set the property, given by the specified key, to the specified
	 * property value. If the specified value is <code>null</code>, the
	 * property will be removed.
	 *
	 * @param key The property key. Must be not <code>null</code>.
	 * @param value The property value or <code>null</code>
	 */
	public void setProperty(String key, String value);
}
