/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represent the root C element corresponding to the workspace.
 * Since there is only one such root element, it is commonly referred to as
 * <em>the</em> C model element.
 * The C model element needs to be opened before it can be navigated or manipulated.
 * The C model element has no parent (it is the root of the C element
 * hierarchy). Its children are {@code ICProject}s.
 * <p>
 * This interface provides methods for performing copy, move, rename, and
 * delete operations on multiple C elements.
 *
 * @see CoreModel#create(org.eclipse.core.resources.IWorkspaceRoot)
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICModel extends ICElement, IParent, IOpenable {
	/**
	 * Copies the given elements to the specified container(s).
	 * If one container is specified, all elements are copied to that
	 * container. If more than one container is specified, the number of
	 * elements and containers must match, and each element is copied to
	 * its associated container.
	 * <p>
	 * Optionally, each copy can positioned before a sibling
	 * element. If {@code null} is specified for a given sibling, the copy
	 * is inserted as the last child of its associated container.
	 * <p>
	 * Optionally, each copy can be renamed. If
	 * {@code null} is specified for the new name, the copy
	 * is not renamed.
	 * <p>
	 * Optionally, any existing child in the destination container with
	 * the same name can be replaced by specifying {@code true} for
	 * force. Otherwise an exception is thrown in the event that a name
	 * collision occurs.
	 *
	 * @param elements the elements to copy
	 * @param containers the container, or list of containers
	 * @param siblings the list of sibling elements, any of which may be
	 *     {@code null}, or {@code null}
	 * @param renamings the list of new names, any of which may be
	 *     {@code null}, or {@code null}
	 * @param replace {@code true} if any existing child in a target container
	 *     with the target name should be replaced, and {@code false} to throw an
	 *     exception in the event of a name collision
	 * @param monitor a progress monitor
	 * @exception CModelException if an element could not be copied. Reasons include:
	 * <ul>
	 * <li> A specified element, container, or sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A {@code CoreException} occurred while updating an underlying resource
	 * <li> A container is of an incompatible type ({@code INVALID_DESTINATION})
	 * <li> A sibling is not a child of it associated container ({@code INVALID_SIBLING})
	 * <li> A new name is invalid ({@code INVALID_NAME})
	 * <li> A child in its associated container already exists with the same
	 * 		name and {@code replace} has been specified as {@code false}
	 *      ({@code NAME_COLLISION})
	 * <li> A container or element is read-only ({@code READ_ONLY})
	 * </ul>
	 */
	void copy(ICElement[] elements, ICElement[] containers, ICElement[] siblings, String[] renamings, boolean replace,
			IProgressMonitor monitor) throws CModelException;

	/**
	 * Deletes the given elements, forcing the operation if necessary and specified.
	 *
	 * @param elements the elements to delete
	 * @param force a flag controlling whether underlying resources that are not
	 *     in sync with the local file system will be tolerated
	 * @param monitor a progress monitor
	 * @exception CModelException if an element could not be deleted. Reasons include:
	 * <ul>
	 * <li> A specified element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A {@code CoreException} occurred while updating an underlying resource
	 * <li> An element is read-only ({@code READ_ONLY})
	 * </ul>
	 */
	void delete(ICElement[] elements, boolean force, IProgressMonitor monitor) throws CModelException;

	/**
	 * Moves the given elements to the specified container(s).
	 * If one container is specified, all elements are moved to that
	 * container. If more than one container is specified, the number of
	 * elements and containers must match, and each element is moved to
	 * its associated container.
	 * <p>
	 * Optionally, each element can positioned before a sibling
	 * element. If {@code null} is specified for sibling, the element
	 * is inserted as the last child of its associated container.
	 * <p>
	 * Optionally, each element can be renamed. If
	 * {@code null} is specified for the new name, the element
	 * is not renamed.
	 * <p>
	 * Optionally, any existing child in the destination container with
	 * the same name can be replaced by specifying {@code true} for
	 * force. Otherwise an exception is thrown in the event that a name
	 * collision occurs.
	 *
	 * @param elements the elements to move
	 * @param containers the container, or list of containers
	 * @param siblings the list of siblings element any of which may be
	 *     {@code null}; or {@code null}
	 * @param renamings the list of new names any of which may be
	 *     {@code null}; or {@code null}
	 * @param replace {@code true} if any existing child in a target container
	 *     with the target name should be replaced, and {@code false} to throw an
	 *     exception in the event of a name collision
	 * @param monitor a progress monitor
	 * @exception CModelException if an element could not be moved. Reasons include:
	 * <ul>
	 * <li> A specified element, container, or sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A {@code CoreException} occurred while updating an underlying resource
	 * <li> A container is of an incompatible type ({@code INVALID_DESTINATION})
	 * <li> A sibling is not a child of it associated container ({@code INVALID_SIBLING})
	 * <li> A new name is invalid ({@code INVALID_NAME})
	 * <li> A child in its associated container already exists with the same
	 * 		name and {@code replace} has been specified as {@code false}
	 *      ({@code NAME_COLLISION})
	 * <li> A container or element is read-only ({@code READ_ONLY})
	 * </ul>
	 *
	 * @exception IllegalArgumentException any element or container is {@code null}
	 */
	void move(ICElement[] elements, ICElement[] containers, ICElement[] siblings, String[] renamings, boolean replace,
			IProgressMonitor monitor) throws CModelException;

	/**
	 * Renames the given elements as specified.
	 * If one container is specified, all elements are renamed within that
	 * container. If more than one container is specified, the number of
	 * elements and containers must match, and each element is renamed within
	 * its associated container.
	 *
	 * @param elements the elements to rename
	 * @param destinations the container, or list of containers
	 * @param names the list of new names
	 * @param replace {@code true} if an existing child in a target container
	 *     with the target name should be replaced, and {@code false} to throw an
	 *     exception in the event of a name collision
	 * @param monitor a progress monitor
	 * @exception CModelException if an element could not be renamed. Reasons include:
	 * <ul>
	 * <li> A specified element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A {@code CoreException} occurred while updating an underlying resource
	 * <li> A new name is invalid ({@code INVALID_NAME})
	 * <li> A child already exists with the same name and {@code replace} has been specified as
	 *      {@code false} ({@code NAME_COLLISION})
	 * <li> An element is read-only ({@code READ_ONLY})
	 * </ul>
	 */
	void rename(ICElement[] elements, ICElement[] destinations, String[] names, boolean replace,
			IProgressMonitor monitor) throws CModelException;

	/**
	 * Returns the C project with the given name. This is a handle-only method.
	 * The project may or may not exist.
	 *
	 * @param name of the project
	 */
	ICProject getCProject(String name);

	/**
	 * Returns the C projects.
	 */
	ICProject[] getCProjects() throws CModelException;

	/**
	 * Returns an array of non-C resources (i.e. non-C projects) in the workspace.
	 * <p>
	 * Non-C projects include all projects that are closed (even if they have the C nature).
	 *
	 * @return an array of non-C projects contained in the workspace.
	 * @throws CModelException if this element does not exist or if an exception occurs
	 *     while accessing its corresponding resource
	 * @since 2.1
	 */
	Object[] getNonCResources() throws CModelException;

	/**
	 * Returns the workspace associated with this C model.
	 */
	IWorkspace getWorkspace();
}
