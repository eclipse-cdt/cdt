/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;


/**
 * @author jcamelon
 *
 */
public interface IASTBaseSpecifier extends ISourceElementCallbackDelegate {

	public ASTAccessVisibility getAccess(); 
	public boolean isVirtual(); 
	public String getParentClassName(); 
	public IASTTypeSpecifier getParentClassSpecifier() throws ASTNotImplementedException;
	public int                getNameOffset(); 

}
