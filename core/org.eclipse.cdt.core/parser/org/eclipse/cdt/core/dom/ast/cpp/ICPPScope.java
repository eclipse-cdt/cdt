/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * The ICPPScope serves as a mechanism for caching IASTNames and bindings to
 * speed up resolution.
 * 
 * @author aniefer
 */
public interface ICPPScope extends IScope {

    /**
	 * Add an IASTName to be cached in this scope
	 * 
	 * @param name
	 * @throws DOMException
	 */
	public void addName(IASTName name) throws DOMException;

	/**
	 * Get the binding that the given name would resolve to in this scope. Could
	 * return null if there is no matching binding in this scope, or if resolve ==
	 * false and the appropriate binding has not yet been resolved.
	 * 
	 * @param name
	 * @param resolve :
	 *            whether or not to resolve the matching binding if it has not
	 *            been so already.
	 * @return : the binding in this scope that matches the name, or null
	 * @throws DOMException
	 */
	public IBinding getBinding(IASTName name, boolean resolve)
			throws DOMException;

	/**
	 * Set whether or not all the names in this scope have been cached
	 * 
	 * @param b
	 */
	public void setFullyCached(boolean b) throws DOMException;

	/**
	 * whether or not this scope's cache contains all the names
	 * 
	 * @return
	 */
	public boolean isFullyCached() throws DOMException;
}
