/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This is the reader interface to the PDOM. It is used by general
 * clients that need access to the information stored there.
 * 
 * @author Doug Schaefer
 */
public interface IPDOM extends IAdaptable {

	/**
	 * Find all the bindings that match the pattern.
	 * 
	 * @param pattern
	 * @return
	 * @throws CoreException
	 */
	public IBinding[] findBindings(Pattern pattern) throws CoreException;
	
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
	
	public void acquireReadLock() throws InterruptedException;
	public void releaseReadLock();
	public void acquireWriteLock() throws InterruptedException;
	public void releaseWriteLock();
	
}
