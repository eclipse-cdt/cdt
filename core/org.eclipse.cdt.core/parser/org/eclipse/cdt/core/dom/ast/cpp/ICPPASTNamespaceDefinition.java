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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public interface ICPPASTNamespaceDefinition extends IASTDeclaration {
    
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty( "Owned" ); //$NON-NLS-1$
	public static final ASTNodeProperty NAMESPACE_NAME    = new ASTNodeProperty( "Name"); //$NON-NLS-1$
	
	
	public IASTName getName();
	public void setName( IASTName name );

    /**
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public List getDeclarations();
	
	public void addDeclaration( IASTDeclaration declaration );

}
