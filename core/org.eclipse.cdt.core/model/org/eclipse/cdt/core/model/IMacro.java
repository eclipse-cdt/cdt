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
 * Represents a field declared in a type.
 */
public interface IMacro extends ICElement, ISourceManipulation, ISourceReference {
	/**
	 * Returns the Identifier List.
	 * @return String
	 */
	String getIdentifierList();
	/**
	 * Returns the Token Sequence.
	 * @return String
	 */
	String getTokenSequence();
}
