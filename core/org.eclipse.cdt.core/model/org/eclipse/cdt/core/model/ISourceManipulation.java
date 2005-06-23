/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

 
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Common protocol for C elements that support source code manipulations such
 * as copy, move, rename, and delete.
 */
public interface ISourceManipulation {
	/**
	 * Copies this element to the given container.
	 *
	 * @param container the container
	 * @param sibling the sibling element before which the copy should be inserted,
	 *   or <code>null</code> if the copy should be inserted as the last child of
	 *   the container
	 * @param rename the new name for the element, or <code>null</code> if the copy
	 *   retains the name of this element
	 * @param replace <code>true</code> if any existing child in the container with
	 *   the target name should be replaced, and <code>false</code> to throw an
	 *   exception in the event of a name collision
	 * @param monitor a progress monitor
	 * @exception CModelException if this element could not be copied. Reasons include:
	 * <ul>
	 * <li> This C element, container element, or sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The container is of an incompatible type (INVALID_DESTINATION)
	 * <li> The sibling is not a child of the given container (INVALID_SIBLING)
	 * <li> The new name is invalid (INVALID_NAME)
	 * <li> A child in the container already exists with the same name (NAME_COLLISION)
	 *		and <code>replace</code> has been specified as <code>false</code>
	 * <li> The container or this element is read-only (READ_ONLY) 
	 * </ul>
	 *
	 * @exception IllegalArgumentException if container is <code>null</code>
	 */
	void copy(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws CModelException;

	/**
	 * Deletes this element, forcing if specified and necessary.
	 *
	 * @param force a flag controlling whether underlying resources that are not
	 *    in sync with the local file system will be tolerated (same as the force flag
	 *	  in IResource operations).
	 * @param monitor a progress monitor
	 * @exception CModelException if this element could not be deleted. Reasons include:
	 * <ul>
	 * <li> This C element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource (CORE_EXCEPTION)</li>
	 * <li> This element is read-only (READ_ONLY)</li>
	 * </ul>
	 */
	void delete(boolean force, IProgressMonitor monitor) throws CModelException;

	/**
	 * Moves this element to the given container.
	 *
	 * @param container the container
	 * @param sibling the sibling element before which the element should be inserted,
	 *   or <code>null</code> if the element should be inserted as the last child of
	 *   the container
	 * @param rename the new name for the element, or <code>null</code> if the
	 *   element retains its name
	 * @param replace <code>true</code> if any existing child in the container with
	 *   the target name should be replaced, and <code>false</code> to throw an
	 *   exception in the event of a name collision
	 * @param monitor a progress monitor
	 * @exception CModelException if this element could not be moved. Reasons include:
	 * <ul>
	 * <li> This C element, container element, or sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The container is of an incompatible type (INVALID_DESTINATION)
	 * <li> The sibling is not a child of the given container (INVALID_SIBLING)
	 * <li> The new name is invalid (INVALID_NAME)
	 * <li> A child in the container already exists with the same name (NAME_COLLISION)
	 *		and <code>replace</code> has been specified as <code>false</code>
	 * <li> The container or this element is read-only (READ_ONLY) 
	 * </ul>
	 *
	 * @exception IllegalArgumentException if container is <code>null</code>
	 */

	void move(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws CModelException;

	/**
	 * Renames this element to the given name.
	 *
	 * @param name the new name for the element
	 * @param replace <code>true</code> if any existing element with the target name
	 *   should be replaced, and <code>false</code> to throw an exception in the
	 *   event of a name collision
	 * @param monitor a progress monitor
	 * @exception CModelException if this element could not be renamed. Reasons include:
	 * <ul>
	 * <li> This C element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The new name is invalid (INVALID_NAME)
	 * <li> A child in the container already exists with the same name (NAME_COLLISION)
	 *		and <code>replace</code> has been specified as <code>false</code>
	 * <li> This element is read-only (READ_ONLY) 
	 * </ul>
	 */
	void rename(String name, boolean replace, IProgressMonitor monitor) throws CModelException;
}
