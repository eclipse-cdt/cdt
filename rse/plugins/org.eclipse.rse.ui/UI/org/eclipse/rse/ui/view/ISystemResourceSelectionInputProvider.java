/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 *  David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/
package org.eclipse.rse.ui.view;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemViewInputProvider;

/**
 * Public interface for the system resource selection input provider that is
 * used in the SystemRemoteResourceDialog and the SystemResourceSelectionForm
 * 
 * @since 3.0
 */
public interface ISystemResourceSelectionInputProvider
  extends ISystemViewInputProvider
{
	/**
	 * Gets the associated system connection
	 * @return the system connection
	 */
	public IHost getSystemConnection();

	/**
	 * Indicates whether the input provider should allow new connections
	 * to be created.
	 * @param flag whether new connections should be allowed
	 */
	public void setAllowNewConnection(boolean flag);

	/**
	 * Returns whether multiple connections can be displayed via the
	 * input provider
	 * @return true if multiple connections are allowed
	 */
	public boolean allowMultipleConnections();

	/**
	 * Returns whether new connections can be created from the view
	 * using this input provider.
	 * @return true if new connections are allowed.
	 */
	public boolean allowNewConnection();

	/**
	 * Returns the category for the view using the input provider (i.e. "files")
	 * @return the category
	 */
	public String getCategory();

	/**
	 * Sets the system types allowed for this input provider
	 * @param types the types of systems
	 */
	public void setSystemTypes(IRSESystemType[] types);

	/**
	 * Gets the system types allowed for the associated control
	 * @return the system types
	 */
	public IRSESystemType[] getSystemTypes();

	/**
	 * Sets the associated system connection for the input provider
	 * @param connection the connection
	 * @param onlyConnection whether other connections are allowed
	 */
	public void setSystemConnection(IHost connection, boolean onlyConnection);
}
