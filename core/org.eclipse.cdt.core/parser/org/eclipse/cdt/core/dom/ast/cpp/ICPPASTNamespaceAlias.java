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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public interface ICPPASTNamespaceAlias extends IASTDeclaration {

    public static final ASTNodeProperty ALIAS_NAME = new ASTNodeProperty( "Alias name"); //$NON-NLS-1$
    public static final ASTNodeProperty MAPPING_NAME = new ASTNodeProperty( "Mapping name"); //$NON-NLS-1$
    
    public IASTName getAlias();
    public void setAlias( IASTName name );
    
    public ICPPASTQualifiedName getQualifiedName();
    public void setQualifiedName( ICPPASTQualifiedName qualifiedName );
    
}
