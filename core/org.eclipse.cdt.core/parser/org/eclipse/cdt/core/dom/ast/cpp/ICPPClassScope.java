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

/**
 * @author aniefer
 */
public interface ICPPClassScope extends ICPPScope {
    /**
     * Get the binding for the class this scope is associated with
     * @return
     */
	ICPPClassType getClassType();
	
	/**
	 * Returns an array of methods that were implicitly added to this class scope.
	 * These methods may or may not have been explicitly declared in the code.
	 * The methods that will be implicitly declared are: the default constructor, 
	 * copy constructor, copy assignment operator, and destructor
	 * @return
	 */
	public ICPPMethod [] getImplicitMethods();
}
