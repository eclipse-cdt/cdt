/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Johann Draschwandtner (Wind River) - [227509][apidoc] Add note how to persist property sets
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 * David McKnight   (IBM)        - [338510] "Copy Connection" operation deletes the registered property set in the original connection
 *******************************************************************************/

package org.eclipse.rse.core.model;

/**
 * A property set container is capable of containing property sets. This interface allows for the
 * addition, retrieval, and deletion of property sets from the container. A property set may have only
 * one container.
 * @noimplement This interface is not intended to be implemented by clients.
 * The standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IPropertySetContainer {

	/**
	 * Retrieves an array all property sets known to this container. It will return an empty array if this
	 * container has property sets. The order of these property sets is not dictated by the interface.
	 * @return an array of property sets.
	 */
	public IPropertySet[] getPropertySets();

	/**
	 * Retrieves a particular property set by its name.
	 * @param name the name of the property set.
	 * @return the named property set or null if one by that name does not exist.
	 */
	public IPropertySet getPropertySet(String name);

	/**
	 * Creates a new property set of a particular name in this container.
	 *
	 * If one already exists by this name, it is replaced with a new empty
	 * property set.
	 *
	 * In order to have the property set persisted, the implementing class
	 * should also implement {@link IRSEPersistableContainer}. The
	 * {@link IRSEPersistableContainer#commit()} method must then be used to
	 * commit any changes into persistent storage.
	 *
	 * @param name the name of the property set.
	 * @return The property set.
	 */
	public IPropertySet createPropertySet(String name);

	/**
	 * Creates a new property set of a particular name and description in this
	 * container. If one already exists by this name it is replaced with a new
	 * empty property set.
	 * 
	 * In order to have the property set persisted, the implementing class
	 * should also implement {@link IRSEPersistableContainer}. The
	 * {@link IRSEPersistableContainer#commit()} method must then be used to
	 * commit any changes into persistent storage.
	 * 
	 * @param name the name of the property set.
	 * @param description the description (usually already localized) for this
	 *            property set.
	 * @return the newly created property set.
	 */
	public IPropertySet createPropertySet(String name, String description);

	/**
	 * Adds an existing property set to this container. If the property set already has a container
	 * it is removed from that container and added to this one. If this container already
	 * has a property set by this name, this one replaces it.
	 * @param set the property set to be added.
	 * @return true if the property set was added.
	 */
	public boolean addPropertySet(IPropertySet set);

	/**
	 * Adds a number of existing property sets to this container.
	 * @param sets the sets to be added
	 * @return true if all property sets were added.
	 * @see #addPropertySet(IPropertySet)
	 */
	public boolean addPropertySets(IPropertySet[] sets);

	/**
	 * Removes a particular property set from this container.
	 * @param name the name of the property set to be removed
	 * @return true if the property set was removed;
	 * false if a property set was not removed, usually if it does not exist in the container.
	 */
	public boolean removePropertySet(String name);
	
	/**
     * Make copies of a list of property sets and add them to the specified container.
     * Each property set may contain its own list of property sets, so the
     * method is recursive.
     * @param targetContainer new container to copy property sets into
	 * @since 3.2
     */
    public void clonePropertySets(IPropertySetContainer targetContainer);

}
