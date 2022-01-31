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
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

/**
 * A connection property is some descriptive information that's discovered about the connection.
 * This service provides property values for a connection.
 *
 * Examples include:
 * <pre>
 * os.name			Operating system name
 * os.arch			Operating system architecture
 * os.version		Operating system version
 * file.separator	File separator ("/" on UNIX)
 * path.separator	Path separator (":" on UNIX)
 * line.separator	Line separator ("\n" on UNIX)
 * user.home		Home directory
 * </pre>
 *
 * @since 2.0
 */
public interface IRemoteConnectionPropertyService extends IRemoteConnection.Service {

	/**
	 * Gets the remote system property indicated by the specified key. The connection must be open prior to calling this method.
	 *
	 * @param key
	 *            the name of the property
	 * @return the string value of the property, or null if no property has that key
	 */
	public String getProperty(String key);

}
