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
 * @author Doug Schaefer
 */
public interface IASTType {

	/**
	 * @return the declarations that introduce this type
	 */
	public int numDeclarations();
	public IASTTypeDeclaration getDeclaration(int i);

	/**
	 * @return the references to this type in this AST
	 */
	public int numReferences();
	public IASTTypeReference getReference(int i);
	
}
