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

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * @author Doug Schaefer
 */
public interface ICPPASTDeclSpecifier extends IASTDeclSpecifier {

	// Extra storage class in C++
	public static final int sc_mutable = IASTDeclSpecifier.sc_last + 1;

	// A declaration in C++ can be a friend declaration
	public boolean isFriend();
	
}
