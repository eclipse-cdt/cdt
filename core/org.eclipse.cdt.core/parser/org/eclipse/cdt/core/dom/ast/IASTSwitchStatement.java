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
 * The switch statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTSwitchStatement extends IASTStatement {

	public static final ASTNodeProperty CONTROLLER = new ASTNodeProperty("controller");  //$NON-NLS-1$
	public static final ASTNodeProperty BODY = new ASTNodeProperty("body");  //$NON-NLS-1$

	/**
	 * This returns the expression which determines which case to take.
	 * 
	 * @return the controller expression
	 */
	public IASTExpression getController();
	
	public void setController(IASTExpression controller);
	
	/**
	 * The body of the switch statement.
	 * 
	 * TODO - finding the cases could be a logical thing
	 * 
	 * @return
	 */
	public IASTStatement getBody();

	public void setBody(IASTStatement body);
	
}
