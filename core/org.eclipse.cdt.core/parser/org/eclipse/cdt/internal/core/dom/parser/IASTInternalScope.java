/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/** 
 * Interface for methods on scopes that are internal to the AST.
 * @since 4.0
 */
public interface IASTInternalScope {
    /**
     * Return the physical IASTNode that this scope was created for
     * @return
     */
    public IASTNode getPhysicalNode() throws DOMException;

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
	
	/** 
	 * clear the name cache in this scope
	 * @throws DOMException
	 */
	public void flushCache() throws DOMException;
}
