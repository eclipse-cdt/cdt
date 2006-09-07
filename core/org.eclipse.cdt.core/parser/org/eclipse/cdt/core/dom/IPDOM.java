/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is the reader interface to the PDOM. It is used by general
 * clients that need access to the information stored there.
 * 
 * @author Doug Schaefer
 */
public interface IPDOM extends IAdaptable {

	/**
	 * Find all the bindings whose names that match the pattern.
	 * 
	 * @param pattern
	 * @return
	 * @throws CoreException
	 */
	public IBinding[] findBindings(Pattern pattern, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Find all bindings whose qualified names match the array of patterns. 
	 * 
	 * @param pattern
	 * @return
	 * @throws CoreException
	 */
	public IBinding[] findBindings(Pattern[] pattern, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Recursively visit the nodes in this PDOM using the given visitor.
	 * 
	 * @param visitor
	 * @throws CoreException
	 */
	public void accept(IPDOMVisitor visitor) throws CoreException;
	
	/** 
	 * Clear all the contents of this PDOM.
	 * 
	 * @throws CoreException
	 */
	public void clear() throws CoreException;
	
	/**
	 * Looks to see if anything has been stored in this PDOM.
	 * 
	 * @return is the PDOM empty
	 * @throws CoreException
	 */
	public boolean isEmpty() throws CoreException;
	
	public ICodeReaderFactory getCodeReaderFactory();
	
	public ICodeReaderFactory getCodeReaderFactory(IWorkingCopy root);
	
	/**
	 * When accessing a PDOM and working with its objects it's neccessary to hold
	 * a read-lock on the PDOM. Make sure to release it: <pre> 
	 * pdom.acquireReadLock(); 
	 * try {
	 *     // do what you have to do.
	 * }
	 * finally {
	 *     pdom.releaseReadLock();
	 * } </pre>
	 * @throws InterruptedException
	 * @since 4.0
	 */
	public void acquireReadLock() throws InterruptedException;
	public void releaseReadLock();
	
	/**
	 * You must not hold any other lock on any PDOM when acquiring a write lock.
	 * Failing to do so may lead to dead-locks.
	 */
	public void acquireWriteLock() throws InterruptedException;
	public void releaseWriteLock();
	
}
