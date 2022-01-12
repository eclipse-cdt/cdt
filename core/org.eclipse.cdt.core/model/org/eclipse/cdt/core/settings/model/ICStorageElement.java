/*******************************************************************************
 * Copyright (c) 2010, 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.CoreException;

/**
 * This interface represents an generic element in a storage tree.  These trees are rooted at
 * {@link ICSettingsStorage} elements.
 *
 * This abstract storage mechanism is used, e.g. with the {@link ICProjectDescription} and
 * {@link ICConfigurationDescription} for storing custom data in the settings file (.cproject)
 * or in a database.
 *
 * @see ICSettingsStorage
 * @see ICProjectDescription
 * @see ICConfigurationDescription
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICStorageElement {
	/**
	 * Return the String of attribute value for name.
	 * If attribute is not found (hasAttribute(name) is false)
	 * this method returns null
	 * @param name
	 * @return String value or null if hasAttribute is false
	 */
	String getAttribute(String name);

	/**
	 * Return whether this ICStorageElement contains an attribute value
	 * for name
	 * @param name
	 * @return boolean indicating existence of attribute with name name
	 * @since 5.1
	 */
	boolean hasAttribute(String name);

	/**
	 * Returns a string array of attribute names
	 */
	String[] getAttributeNames();

	/**
	 * Return the parent IStorageElement or null if this
	 * ICStorageElement doesn't have a parent
	 * @return ICStorageElement parent or null
	 */
	ICStorageElement getParent();

	/**
	 * Set an attribute on this ICStorageElement
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, String value);

	/**
	 * Remove an attribute from this ICStorageElement
	 * @param name
	 */
	void removeAttribute(String name);

	/**
	 * Create a child ICStorageElement with the given name.
	 * @param name
	 * @return new ICStorageElement representing the child
	 */
	ICStorageElement createChild(String name);

	/**
	 * Returns an array of the ICStorageElement children of this
	 * ICStorageElement or an empty array if no children were found
	 * @return ICStorageElement[] of children or empty array
	 */
	ICStorageElement[] getChildren();

	/**
	 * Returns the children ICStorageElements with name name
	 * @param name String name of children to be returned
	 * @return ICStorageElement[] of children may be the empty list if no children with name found
	 * @since 5.1
	 */
	ICStorageElement[] getChildrenByName(String name);

	/**
	 * Returns true if this storage element has child ICStorageElements
	 * @return boolean indicating whether this ICStorageElement has children
	 * @since 5.1
	 */
	boolean hasChildren();

	/**
	 * Erases all children, attributes and any value set on this ICStorageElement
	 */
	void clear();

	/**
	 * Returns the name of this ICStorageElement
	 */
	String getName();

	/**
	 * Removes the ICStorageElement from the set of child ICSotrageElements
	 * @param el
	 */
	void removeChild(ICStorageElement el);

	/**
	 * Returns the String value of this element or null if there is no String value set.
	 *
	 * NB a pure whitespace value is considered to be null
	 * @return String or null
	 */
	String getValue();

	/**
	 * Sets a String value on the ICStorageElement
	 * @param value
	 */
	void setValue(String value);

	/**
	 * Imports an existing ICStorageElemtn as a child of this ICStorageElement
	 * @param element
	 * @return ICStorageElement a handle on the newly imported ICStorageElement
	 */
	ICStorageElement importChild(ICStorageElement element) throws UnsupportedOperationException;

	/**
	 * Create a deep copy of the current ICStorageElement such that name, children, attributes and value
	 * are the same.
	 * <br />
	 * However this is implemented it should appear to the user that a deep copy of
	 * the elements within has occurred.  [ Though the implementation may be copy-on-write
	 * if the underlying data structure is suitable. ]
	 * <br /><br />
	 * getParent() of the clone should be equal to the original element.getParent().
	 * However the clone() doesn't appear in the parent's getChildren() array.
	 * @return ICStorageElement deep copy of this ICStorageElement
	 * @since 5.1
	 */
	ICStorageElement createCopy() throws UnsupportedOperationException, CoreException;

	/**
	 * Tests whether this storage element is exactly equal to other
	 * To be equal all name, children attributes and value must be
	 * equal between the two ICStorageElements
	 * @param other
	 * @return boolean indicating equality
	 * @since 5.1
	 */
	boolean equals(ICStorageElement other);
}
