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
public interface IASTCompoundStatement extends IASTStatement {

	/**
	 * @return the first statement in this compound statement
	 */
	public IASTStatement getFirstStatement();
	
	/**
	 * Append a statement to the list of statements in this compound statement
	 * 
	 * @param statement
	 */
	public void appendStatement(IASTStatement statement);
	
}
