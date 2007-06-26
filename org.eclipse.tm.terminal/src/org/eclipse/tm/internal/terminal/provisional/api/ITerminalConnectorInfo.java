/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

/**
 * This class is a handle to a {@link ITerminalConnector connector} that comes from an
 * extension. It maintains a proxy to the connector to allow lazy initialization of the
 * real {@link ITerminalConnector connector} that comes from an extension.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 */
public interface ITerminalConnectorInfo {
	/**
	 * @return an ID of this connector. The id from the plugin.xml.
	 * <p>Note: return <code>null</code> because the framework takes 
	 * care to get the value from the plugin.xml
	 */
	String getId();

	/**
	 * @return <code>null</code> the name (as specified in the plugin.xml)
	 * <p>Note: return <code>null</code> because the framework takes 
	 * care to get the value from the plugin.xml
	 */
	String getName();

	/**
	 * @return true if the ITerminalConnector has been initialized.
	 * If there was an initialization error, {@link #getInitializationErrorMessage()}
	 * returns the error message.
	 */
	boolean isInitialized();
	
	/**
	 * This method initializes the connector if it is not initialized!
	 * If the connector was initialized successfully, <code>null</code> is
	 * returned. Otherwise an error message describing the problem is returned.
	 * @return <code>null</code> or a localized error message.
	 */
	String getInitializationErrorMessage();
	
	/**
	 * Returns a proxy to the connector that is lazily initialized.
	 * The following methods can be called without initializing
	 * the contributed class: 
	 * {@link ITerminalConnector#getSettingsSummary()}, {@link ITerminalConnector#load(ISettingsStore)},
	 * {@link ITerminalConnector#save(ISettingsStore)}, {@link ITerminalConnector#setTerminalSize(int, int)}
	 * @return a proxy of the real connector. Some calls initialize the the connection.
	 */
	ITerminalConnector getConnector();
}
