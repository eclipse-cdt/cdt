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
package org.eclipse.cdt.core.dom.ast.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;

/**
 * This is an initializer that is a call to the constructor for the
 * declarator.
 * 
 * @author Doug Schaefer
 */
public interface ICPPASTConstructorInitializer extends IASTInitializer {

	/**
	 * Get the arguments to the constructor.
	 * 
	 * @return List of IASTExpression
	 */
	public List getExpressions();
	
}
