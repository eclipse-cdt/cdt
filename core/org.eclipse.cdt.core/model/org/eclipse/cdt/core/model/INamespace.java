/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
    IBM Rational Software - Initial API and implementation
**********************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Represents a package declaration in a C translation unit.
 */
public interface INamespace extends ICElement, IParent, ISourceManipulation, ISourceReference {
	/**
	 * Returns the typename of a namespace.
	 * @return String
	 */
	String getTypeName();
}
