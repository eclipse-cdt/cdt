/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An openable is an element that can be opened, saved, and closed.
 * An openable might or might not have an associated buffer.
 */
public interface IOpenable extends IBufferChangedListener{
	/**
	 * Closes this element and its buffer (if any).
	 */
	public void close() throws CModelException;
	/**
	 * Returns the buffer opened for this element, or <code>null</code>
	 * if this element does not have a buffer.
	 */
	public IBuffer getBuffer() throws CModelException;
	/**
	 * returns true if the associated buffer has some unsaved changes 
	 */
	boolean hasUnsavedChanges() throws CModelException;
	/**
	 * Returns whether the element is consistent with its underlying resource or buffer.
	 * The element is consistent when opened, and is consistent if the underlying resource
	 * or buffer has not been modified since it was last consistent.
	 */
	boolean isConsistent() throws CModelException;

	/**
	 * Returns whether this CFile is open.
	 */
	boolean isOpen();
	
	/**
	 * Makes this element consistent with its underlying resource or buffer 
	 * by updating the element's structure and properties as necessary.
	 */
	void makeConsistent(IProgressMonitor progress) throws CModelException;
	void makeConsistent(IProgressMonitor progress, boolean forced) throws CModelException;

	/**
	 * Opens this element and all parent elements that are not already open.
	 * For translation units, a buffer is opened on the contents of the
	 * underlying resource.
	 */
	public void open(IProgressMonitor progress) throws CModelException;
	
	/**
	 * Saves any changes in this element's buffer to its underlying resource
	 * via a workspace resource operation. 
	 * <p>
	 * The <code>force</code> parameter controls how this method deals with
	 * cases where the workbench is not completely in sync with the local file system.
	 */
	public void save(IProgressMonitor progress, boolean force) throws CModelException;

}
