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
 * This represents a block of statements.
 * 
 * @author Doug Schaefer
 */
public interface IASTCompoundStatement extends IASTStatement {

	ASTNodeProperty NESTED_STATEMENT = new ASTNodeProperty( "Nested Statement" ); //$NON-NLS-1$

    /**
	 * Gets the statements in this block.
	 * 
	 * @return List of IASTStatement
	 */
	public IASTStatement[] getStatements();
	
	public void addStatement( IASTStatement statement );
	
	public IScope getScope();
}
