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

/**
 * @author jcamelon
 */
public interface ICPPASTLinkageSpecification extends IASTDeclaration {

    public String getLiteral();
    public void setLiteral( String value );
    
    public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty( "Owned Declaration"); //$NON-NLS-1$
    public IASTDeclaration [] getDeclarations();
    public void addDeclaration( IASTDeclaration declaration );
}
