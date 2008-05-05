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
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/
package org.eclipse.rse.ui.view;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemViewInputProvider;

/**
 * @since 3.0
 */
public interface ISystemSelectRemoteObjectAPIProvider
		extends ISystemViewInputProvider
{
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 */
	public Object[] getConnectionChildren(IHost selectedConnection);

	/**
	 * Get the name of the item to select when the first filter is expanded.
	 * Called by the filter adapter.
	 */
	public String getPreSelectFilterChild();

	/**
	 * Get the actual object of the item to select when the first filter is expanded.
	 * Called by the GUI form after expansion, so it can select this object
	 */
	public Object getPreSelectFilterChildObject();

	/**
	 * Set the filter string to use to resolve the inputs.
	 * If this is an absolute filter string, it gets turned into a quick filter string,
	 *  so that the user sees it and can expand it. If it is a relative filter string
	 *  to apply to all expansions, it is used to decorate all filtering as the user drills down.
	 */
	public void setFilterString(String string);

	/**
	 * Set actual child object of the first filter to preselect. Called
	 * by the filter adapter once the children are resolved and a match on
	 * the name is found.
	 */
	public void setPreSelectFilterChildObject(Object obj);

	/**
	 * Set child of the first filter to preselect
	 */
	public void setPreSelectFilterChild(String name);

	/**
	 * Set the quick filters to be exposed to the user. These will be shown to the
	 *  user when they expand a connection.
	 * @see org.eclipse.rse.core.filters.SystemFilterSimple
	 */
	public void setQuickFilters(ISystemFilter[] filters);


	/**
	 * Specify whether the user should see the "New Connection..." special connection prompt
	 */
	public void setShowNewConnectionPrompt(boolean show);


	/**
	 * Default or Restrict to a specific connection.
	 * If default mode, it is preselected.
	 * If only mode, it is the only connection listed.
	 * @param connection The connection to default or restrict to
	 * @param onlyMode true if this is to be the only connection shown in the list
	 */
	public void setSystemConnection(IHost connection, boolean onlyMode);


	/**
	 * Specify system types to restrict what types of connections
	 * the user can create, and see.
	 * This will override subsystemConfigurationId,if that has been set!
	 *
     * @param systemTypes An array of system types, or
     *     <code>null</code> to allow all registered valid system types.
     *     A system type is valid if at least one subsystem configuration
     *     is registered against it.
	 */
	public void setSystemTypes(IRSESystemType[] systemTypes);
}
