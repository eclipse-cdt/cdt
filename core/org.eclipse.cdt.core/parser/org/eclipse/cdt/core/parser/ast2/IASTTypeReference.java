/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2;

/**
 * Represents a reference to a type. This reference also works in the
 * role of a type. For example, a variable may introduce a type itself
 * (e.g. struct { ... } a;) or may refer to a type (struct foo a;).
 * 
 * @author Doug Schaefer
 */
public interface IASTTypeReference extends IASTReference, IASTType {

	/**
	 * @return the type being refered to
	 */
	public IASTType getType();
	
}
