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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author Doug Schaefer
 * 
 * This is the interface to the Persisted DOM (PDOM).
 * It provides services to allow access to DOM information
 * persisted between parses.
 */
public interface IPDOM extends IAdaptable {

	public IBinding resolveBinding(IASTName name);
	
	public IBinding[] findBindings(String pattern) throws CoreException;
	
	public IASTName[] getDeclarations(IBinding binding);
	
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
	
	public IPDOMIndexer getIndexer();
	public void setIndexer(IPDOMIndexer indexer) throws CoreException;
	
	public void acquireReadLock() throws InterruptedException;
	public void releaseReadLock();
	public void acquireWriteLock() throws InterruptedException;
	public void releaseWriteLock();
	
}
