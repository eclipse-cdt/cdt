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
package org.eclipse.cdt.core.dom.ast;



/**
 * This is the declarator for an array.
 * 
 * @author Doug Schaefer
 */
public interface IASTArrayDeclarator extends IASTDeclarator {

    public static final ASTNodeProperty ARRAY_MODIFIER = new ASTNodeProperty( "Array Modifier"); //$NON-NLS-1$
    
	public IASTArrayModifier[] getArrayModifiers();
	public void addArrayModifier( IASTArrayModifier arrayModifier );
	
}
