/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;




/**
 * A C Folder Resource.
 */
public interface ICContainer extends ICElement, IParent, IOpenable {

	/**
	 * Returns an array of non-C resources directly contained in this project.
	 * It does not transitively answer non-C resources contained in folders;
	 * these would have to be explicitly iterated over.
	 * <p>
	 * Non-C resources includes files, folders, projects  not accounted for.
	 * </p>
	 * 
	 * @return an array of non-C resources directly contained in this project
	 * @exception JavaModelException if this element does not exist or if an
	 *              exception occurs while accessing its corresponding resource
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
	 * Returns the tranlation unit with the specified name
	 * in this container (for example, <code>"foobar.c"</code>).
	 * The name has to be a valid translation unit name.
	 * This is a handle-only operation.  The celement
	 * may or may not exist.
	 * 
	 * @param name the given name
	 * @return the translation unit with the specified name in this container
	 */
	ITranslationUnit getTranslationUnit(String name);

	/**
	 * Returns the all the binaries of this container.
	 * 
	 * @return
	 * @throws CModelException
	 */
	IBinary[] getBinaries() throws CModelException;

	/**
	 * Return the binary for this name, it must be a
	 * valid binary
	 * This is a handle-only operation.  The celement
	 * may or may not exist.
	 * 
	 * @return
	 * @throws CModelException
	 */
	IBinary getBinary(String name);

	/**
	 * Returns all the archive of this container
	 * 
	 * @return
	 * @throws CModelException
	 */
	IArchive[] getArchives() throws CModelException;

	/**
	 * This is a handle-only operation.  The celement
	 * may or may not exist.
	 * 
	 * @param file
	 * @return
	 * @throws CModelException
	 */
	IArchive getArchive(String name);

	/**
	 * Return al the child containers of this container.
	 * 
	 * @return
	 * @throws CModelException
	 */
	ICContainer[] getCContainers() throws CModelException;

	/**
	 * Returns the container with the given name.
	 * An empty string indicates the default package.
	 * This is a handle-only operation.  The celement
	 * may or may not exist.
	 * 
	 * @param name the given container
	 * @return the container with the given name
	 */
	ICContainer getCContainer(String name);

}
