package org.eclipse.cdt.core.parser;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

public interface IScannerInfoProvider {
	
	/**
	 * The receiver will register the listener specified in the argument
	 * to receive change notifications when the information for the 
	 * <code>IResource</code> it is responsible for changes. 
	 *  
	 * @param listener
	 */
	public void subscribe(IResource resource, IScannerInfoChangeListener listener);
	
	/**
	 * Answers <code>true</code> if the receiver has information for
	 * the resource specified in the argument, else <code>false</code>. 
	 * 
	 * @param resource
	 * @return
	 */
	public boolean managesResource(IResource resource) throws CoreException;
	
	/**
	 * The receiver will no longer notify the listener specified in 
	 * the argument when information about the reource it is responsible 
	 * for changes.
	 * 
	 * @param listener
	 */
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener);
}
