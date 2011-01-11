/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 * Tom Hochstein (Freescale)     - [301075] Host copy doesn't copy contained property sets
 *******************************************************************************/
package org.eclipse.rse.tests.core.connection;

import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;

/**
 * Interfaces declares public access and factory methods to deal
 * with RSE connections and artefacts.
 */
public interface IRSEConnectionManager {

	/**
	 * Loads the connection properties from the specified file. The
	 * file must exist and must be a valid formated properties file.
	 * <p>
	 * Note: The loaded properties will be underlayed by a set of default
	 *       properties. The default properties will be loaded from the
	 *       file <code>&lt;bundle_location&gt;/src/org/eclipse/rse/tests/internal/connectionDefault.properties</code>.
	 * <p>
	 * @param path The properties file location. Must be not <code>null</code>.
	 * @param allowDefaults Specify <code>true</code> to allow to underlay the connection properties with default,
	 *                      <code>false</code> otherwise.
	 *                      
	 * @return The corresponding <code>IRSEConnectionProperties</code> object or <code>null</code>
	 *         if the loading of the properties fails.
	 * 
	 * @see java.util.Properties
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionProperties
	 */
	public IRSEConnectionProperties loadConnectionProperties(IPath path, boolean allowDefaults);
	
	/**
	 * Loads the connection properties using the given set of potential
	 * incomplete properties and the default properties.
	 * 
	 * @param properties The connection properties. Must be not <code>null</code>
	 * @param allowDefaults Specify <code>true</code> to allow to underlay the connection properties with default,
	 *                      <code>false</code> otherwise.
	 * 
	 * @return The corresponding <code>IRSEConnectionProperties</code> object or <code>null</code>
	 *         if the loading of the properties fails.
	 * 
	 * @see java.util.Properties
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionProperties
	 */
	public IRSEConnectionProperties loadConnectionProperties(Properties properties, boolean allowDefaults);
	
	/**
	 * Finds the connection given by the specified name/label from the specified
	 * system profile. The method will do nothing if either the system profile or
	 * the connection does not exist.
	 * 
	 * @param profileName The system profile to look for the connection. Must be not <code>null</code>.
	 * @param name The name of the connection to find. Must be not <code>null</code>.
	 * @return The found connection, or null if failed.
	 */
	public IHost findConnection(String profileName, String name);

	/**
	 * Copies the connection.
	 * 
	 * @param connection The connection to copy. Must be not <code>null</code>.
	 * @param copyName The name of the new connection. Must be not <code>null</code>.
	 * @return The copied connection, or null if failed.
	 */
	public IHost copyConnection(IHost connection, String copyName);

	/**
	 * Removes the connection given by the specified name/label from the specified
	 * system profile. The method will do nothing if either the system profile or
	 * the connection does not exist.
	 * 
	 * @param profileName The system profile to remove the connection from. Must be not <code>null</code>.
	 * @param name The name of the connection to remove. Must be not <code>null</code>.
	 */
	public void removeConnection(String profileName, String name);
	
	/**
	 * Lookup the connection described by the given connection properties. If
	 * the described connection does not exist, the connection (and all required
	 * RSE artifacts) will be created.
	 * 
	 * @param properties The connection properties. Must be not <code>null</code>.
	 * @return The corresponding <code>IHost</code> connection object.
	 */
	public IHost findOrCreateConnection(IRSEConnectionProperties properties);

	/**
	 * Get the file subsystem, matching the specified configuration id, for the specified connection.
	 * 
	 * @param connection The corresponding <code>IHost</code> connection object. Must be not <code>null</code>.
	 * @param desiredConfigurationId The subsystem configuration id of the desired subsystem. Must be not <code>null</code>.
	 *  
	 * @return The file subsystem object if found or <code>null</code>.
	 * 
	 * @throws Exception If the file subsystem lookup fails.
	 */
	public FileServiceSubSystem getFileSubSystem(IHost connection, String desiredConfigurationId) throws Exception;
	
	/**
	 * Get the shell subsystem for the specified connection.
	 *  
	 * @param connection The corresponding <code>IHost</code> connection object. Must be not <code>null</code>.
	 * @return The shell subsystem object if found or <code>null</code>.
	 * 
	 * @throws Exception If the shell subsystem lookup fails.
	 */
	public IShellServiceSubSystem getShellSubSystem(IHost connection) throws Exception;

	
	/**
	 * Get the testsubsystem for the specified connection.
	 *  
	 * @param connection The corresponding <code>IHost</code> connection object. Must be not <code>null</code>.
	 * @return The testsubsystem object if found or <code>null</code>.
	 * 
	 * @throws Exception If the testsubsystem lookup fails.
	 */
	public ITestSubSystem getTestSubSystem(IHost connection) throws Exception;
}
