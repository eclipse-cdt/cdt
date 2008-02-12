/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [186773] split SystemRegistryUI from SystemRegistry implementation
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core from org.eclipse.rse.ui.view
 * Martin Oberhuber (Wind River) - [218524][api] Remove deprecated ISystemViewInputProvider#getShell()
 ********************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Abstraction for any object that wishes to be a roots-provider for the SystemView tree viewer.
 */
public interface ISystemViewInputProvider extends IAdaptable {
	
	/**
	 * Return the child objects to constitute the root elements in the system view tree.
	 * @return Array of root root elements for the system view tree
	 */
	public Object[] getSystemViewRoots();

	/**
	 * Test if {@link #getSystemViewRoots()} will return a non-empty list
	 * @return <code>true</code> if root elements are available, or <code>false</code> otherwise.
	 */
	public boolean hasSystemViewRoots();

	/**
	 * @return true if we are listing connections or not, so we know whether we are interested in 
	 * connection-add events
	 */
	public boolean showingConnections();

	/**
	 * This method is called by the connection adapter when the user expands
	 * a connection. This method must return the child objects to show for that
	 * connection.
	 * @param selectedConnection the connection undergoing expansion
	 * @return the list of objects under the connection
	 */
	public Object[] getConnectionChildren(IHost selectedConnection);

	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection.
	 * @param selectedConnection the connection being shown in the viewer 
	 * @return true if this connection has children to be shown.
	 */
	public boolean hasConnectionChildren(IHost selectedConnection);

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapterType);

	/**
	 * Set the viewer in case it is needed for anything.
	 * The label and content provider will call this.
	 * @param viewer the {@link org.eclipse.jface.viewers.Viewer viewer}
	 *     that uses this provider
	 */
	public void setViewer(Object viewer);

	/**
	 * Return the viewer we are currently associated with
	 * @return the {@link org.eclipse.jface.viewers.Viewer viewer}
	 *     we are currently associated with
	 * @deprecated use other methods for getting the active viewer.
	 */
	public Object getViewer();
}