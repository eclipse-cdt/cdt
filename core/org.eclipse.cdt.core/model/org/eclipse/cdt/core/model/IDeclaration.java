package org.eclipse.cdt.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

public interface IDeclaration extends ICElement, ISourceManipulation, ISourceReference {
	boolean isStatic();
	boolean isConst();
	boolean isVolatile();
	
	/**
	 * Returns the access Control of the member. The access qualifier
	 * can be examine using the AccessControl class.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @see IAccessControl
	 */
	int getAccessControl();	
}
