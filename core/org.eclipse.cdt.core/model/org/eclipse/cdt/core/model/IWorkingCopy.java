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



import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <p> A working copy of a C element acts just like a regular element (handle),
 * except it is not attached to an underlying resource. A working copy is not
 * visible to the rest of the C model. Changes in a working copy's buffer are
 * not realized in a resource. To bring the C model up-to-date with a working
 * copy's contents, an explicit commit must be performed on the working copy.
 * Other operations performed on a working copy update the contents of the
 * working copy's buffer but do not commit the contents of the working copy.
 * </p>
 * <p>
 * Note: The contents of a working copy is determined when a working
 * copy is created, based on the current content of the element the working
 * copy is created from. If a working copy is an <code>ICFile</code> and is
 * explicitly closed, the working copy's buffer will be thrown away. However,
 * clients should not explicitly open and close working copies.
 * </p>
 * <p>
 * The client that creates a working copy is responsible for
 * destroying the working copy. The C model will never automatically destroy or
 * close a working copy. (Note that destroying a working copy does not commit it
 * to the model, it only frees up the memory occupied by the element). After a
 * working copy is destroyed, the working copy cannot be accessed again. Non-
 * handle methods will throw a <code>CModelException</code> indicating the
 * C element does not exist.
 * </p>
 * <p>
 * A working copy cannot be created from another working copy.
 * Calling <code>getWorkingCopy</code> on a working copy returns the receiver.
 * </p>
 */
public interface IWorkingCopy extends ITranslationUnit{
	/**
	 * Commits the contents of this working copy to its original element
	 * and underlying resource, bringing the C model up-to-date with the current
	 * contents of the working copy.
	 *
	 * <p>It is possible that the contents of the original resource have changed
	 * since this working copy was created, in which case there is an update conflict.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the contents of this working copy are applied to
	 * 	the underlying resource even though this working copy was created before
	 *	a subsequent change in the resource</li>
	 * <li> <code>false</code> - in this case a <code>CModelException</code> is
	 * thrown</li>
	 * </ul>
	 */
	void commit(boolean force, IProgressMonitor monitor) throws CModelException;
	/**
	 * Destroys this working copy, closing its buffer and discarding
	 * its structure. Subsequent attempts to access non-handle information
	 * for this working copy will result in <code>CModelException</code>s. Has
	 * no effect if this element is not a working copy.
	 * <p>
	 * If this working copy is shared, it is destroyed only when the number of calls to
	 * <code>destroy()</code> is the same as the number of calls to <code>
	 * getSharedWorkingCopy(IProgressMonitor, IBufferFactory)</code>. 
	 * A REMOVED CElementDelta is then reported on this working copy.
	 */
	void destroy();

	/**
	 * Returns the original element the specified working copy element was created from,
	 * or <code>null</code> if this is not a working copy element.
	 * 
	 * @param workingCopyElement the specified working copy element
	 * @return the original element the specified working copy element was created from,
	 * or <code>null</code> if this is not a working copy element
	 */
	ICElement getOriginal(ICElement workingCopyElement);

	/**
	 * Returns the original element this working copy was created from,
	 * or <code>null</code> if this is not a working copy.
	 */ 
	ITranslationUnit getOriginalElement();
		
	/**
	 * Returns whether this working copy's original element's content
	 * has not changed since the inception of this working copy.
	 * 
	 * @return true if this working copy's original element's content
	 * has not changed since the inception of this working copy, false otherwise
	 */
	boolean isBasedOn(IResource resource);
	
	/**
	 * Reconciles the contents of this working copy.
	 * It performs the reconciliation by locally caching the contents of 
	 * the working copy, updating the contents, then creating a delta 
	 * over the cached contents and the new contents, and finally firing
	 * this delta.
	 * <p>
	 * If the working copy hasn't changed, then no problem will be detected,
	 * this is equivalent to <code>IWorkingCopy#reconcile(false, null)</code>.
	 * <p>
	 */	
	IMarker[] reconcile() throws CModelException;
	
	/**
	 * Reconciles the contents of this working copy.
	 * It performs the reconciliation by locally caching the contents of 
	 * the working copy, updating the contents, then creating a delta 
	 * over the cached contents and the new contents, and finally firing
	 * this delta.
	 * <p>
	 * The boolean argument allows to force problem detection even if the
	 * working copy is already consistent.
	 */
	void reconcile(boolean forceProblemDetection, IProgressMonitor monitor) throws CModelException;

	/**
	 * Restores the contents of this working copy to the current contents of
	 * this working copy's original element. Has no effect if this element
	 * is not a working copy.
	 */	
	void restore() throws CModelException;
}
