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

/**
 * @author jcamelon
 */
public interface ICPPASTTemplateDeclaration extends IASTDeclaration {

    public boolean isExported();
    public void setExported( boolean value );
    
    public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty( "Owned Declaration"); //$NON-NLS-1$
    public IASTDeclaration getDeclaration();
    public void setDeclaration( IASTDeclaration declaration );
    
    public static final ASTNodeProperty PARAMETER = new ASTNodeProperty( "Template Parameter"); //$NON-NLS-1$
    public List getTemplateParameters();
    public void addTemplateParamter( ICPPASTTemplateParameter parm );
}
