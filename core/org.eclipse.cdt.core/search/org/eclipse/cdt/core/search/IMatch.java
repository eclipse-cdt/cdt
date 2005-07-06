/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Interface used for returning matches from the Search Engine
 *
 */
public interface IMatch {
	
	/**
	 * Returns ICElement constant describing the element type
	 * @return
	 */
	int getElementType();

	int getVisibility();

	String getName();

    /**
     * Returns the list of parameters if this matched a function or a method.
     * 
     * @return array of Strings for the parameters
     */
    String[] getParameters();

    /**
     * Returns the return type if this matched a function or a method.
     * 
     * @return the return type
     */
    String getReturnType();
    
	String getParentName();

	IResource getResource();
	
	IPath getLocation();

	IPath getReferenceLocation();
	
	IMatchLocatable getLocatable();
	
	boolean isStatic();
	boolean isConst();
	boolean isVolatile();
}
