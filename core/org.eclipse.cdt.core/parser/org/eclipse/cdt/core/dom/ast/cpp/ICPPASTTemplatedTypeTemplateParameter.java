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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public interface ICPPASTTemplatedTypeTemplateParameter extends
        ICPPASTTemplateParameter {

    public static final ASTNodeProperty PARAMETER = new ASTNodeProperty( "Template Parameter"); //$NON-NLS-1$
    public List getTemplateParameters();
    public void addTemplateParamter( ICPPASTTemplateParameter parm );

    public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty( "Name" ); //$NON-NLS-1$
    public IASTName getName();
    public void setName( IASTName name );

    public static final ASTNodeProperty DEFAULT_VALUE = new ASTNodeProperty( "Default Value"); //$NON-NLS-1$
    public IASTExpression getDefaultValue();
    public void setDefaultValue( IASTExpression expression );
}
