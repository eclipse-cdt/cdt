/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.resources.IContainer;

/**
 * A C folder resource.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICContainer extends ICElement, IParent, IOpenable {
	/**
	 * Returns an array of non-C resources directly contained in this project.
	 * It does not transitively answer non-C resources contained in folders;
	 * these would have to be explicitly iterated over.
	 * <p>
	 * Non-C resources includes files, folders, projects not accounted for.
	 * </p>
	 *
	 * @return an array of non-C resources directly contained in this project
	 * @exception CModelException if this element does not exist or if an
	 *            exception occurs while accessing its corresponding resource
	 */
	Object[] getNonCResources() throws CModelException;

	/**
	 * Returns all of the translation units in this ccontainer.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return all of the translation units in this ccontainer
	 */
	ITranslationUnit[] getTranslationUnits() throws CModelException;

	/**
	 * Returns the translation unit with the specified name in this container
	 * (for example, {@code "foobar.c"}). The name has to be a valid translation unit name.
	 * This is a handle-only operation. The celement may or may not exist.
	 *
	 * @param name the given name
	 * @return the translation unit with the specified name in this container
	 */
	ITranslationUnit getTranslationUnit(String name);

	/**
	 * Returns the all the binaries of this container.
	 */
	IBinary[] getBinaries() throws CModelException;

	/**
	 * Returns the binary for this name, it must be a valid binary.
	 * This is a handle-only operation.  The container may or may not exist.
	 */
	IBinary getBinary(String name);

	/**
	 * Returns all the archives of this container.
	 */
	IArchive[] getArchives() throws CModelException;

	/**
	 * This is a handle-only operation.  The container may or may not exist.
	 */
	IArchive getArchive(String name);

	/**
	 * Returns all the child containers of this container.
	 */
	ICContainer[] getCContainers() throws CModelException;

	/**
	 * Returns the container with the given name. An empty string indicates the default package.
	 * This is a handle-only operation.  The celement may or may not exist.
	 *
	 * @param name the name of a nested container
	 * @return the container with the given name
	 */
	ICContainer getCContainer(String name);

	/**
	 * Returns the corresponding IContainer.
	 * @since 5.9
	 */
	@Override
	IContainer getResource();
}
